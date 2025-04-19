package com.example;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import static org.apache.spark.sql.functions.*;

import org.bson.Document;
import com.mongodb.client.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

import io.github.cdimascio.dotenv.Dotenv;

public class SparkStructuredStreamingApp {

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();  // ✅ .env 로드
        String mongoUri = dotenv.get("MONGO_URI");
        String slackWebhook = dotenv.get("SLACK_WEBHOOK");

        SparkSession spark = SparkSession.builder()
                .appName("KafkaStructuredStreaming")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        Dataset<Row> kafkaStream = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "kafka:9092")
                .option("subscribe", "sensor-stream")
                .option("startingOffsets", "latest")
                .load();

        StructType schema = new StructType()
                .add("sensor_id", DataTypes.StringType)
                .add("temperature", DataTypes.DoubleType)
                .add("timestamp", DataTypes.LongType);

        Dataset<Row> parsed = kafkaStream
                .selectExpr("CAST(value AS STRING) as json")
                .select(from_json(col("json"), schema).as("data"))
                .select("data.*");

        Dataset<Row> avgBySensor = parsed
                .groupBy(col("sensor_id"))
                .agg(avg("temperature").alias("avg_temp"));

        avgBySensor.writeStream()
                .format("console")
                .outputMode("update")
                .option("truncate", false)
                .start();

        avgBySensor.writeStream()
                .foreach(new ForeachWriter<Row>() {
                    private transient MongoClient mongoClient;

                    @Override
                    public boolean open(long partitionId, long version) {
                        try {
                            System.out.println("🔌 Connecting to MongoDB...");
                            mongoClient = MongoClients.create(mongoUri);  // ✅ .env에서 불러온 값 사용
                            return true;
                        } catch (Exception e) {
                            System.out.println("❌ Mongo 연결 실패: " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    public void process(Row row) {
                        String sensorId = row.getString(0);
                        double avg = row.getDouble(1);

                        System.out.printf("📥 [MongoDB Insert] sensor_id: %s, avg_temp: %.2f°C%n", sensorId, avg);

                        Document doc = new Document("sensor_id", sensorId)
                                .append("avg_temp", avg)
                                .append("timestamp", System.currentTimeMillis());
                        mongoClient.getDatabase("iot")
                                .getCollection("avg_temperatures")
                                .insertOne(doc);

                        if (avg > 30.0) {
                            sendSlackAlert(slackWebhook, sensorId, avg);  // ✅ 외부 webhook 전달
                        }
                    }

                    @Override
                    public void close(Throwable errorOrNull) {
                        mongoClient.close();
                    }

                    public void sendSlackAlert(String webhookUrl, String sensorId, double temp) {
                        String msg = String.format("🔥 경고! %s 센서 온도 %.2f°C", sensorId, temp);
                        try {
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(webhookUrl))
                                    .header("Content-Type", "application/json")
                                    .POST(BodyPublishers.ofString("{\"text\": \"" + msg + "\"}"))
                                    .build();
                            client.send(req, HttpResponse.BodyHandlers.ofString());
                        } catch (Exception e) {
                            System.err.println("Slack 알림 실패:");
                            e.printStackTrace();
                        }
                    }
                })
                .outputMode("update")
                .start()
                .awaitTermination();
    }
}
