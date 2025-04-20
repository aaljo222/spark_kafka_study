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
        // Hadoop 홈 디렉토리 설정 (Windows 환경)
        System.setProperty("hadoop.home.dir", "C:\\winutils\\hadoop-3.2.2");

        // .env 환경 변수 로드
        Dotenv dotenv = Dotenv.load();
        String mongoUri = dotenv.get("MONGO_URI");
        String slackWebhook = dotenv.get("SLACK_WEBHOOK");

        System.out.println("✅ MONGO_URI = " + mongoUri);
        System.out.println("✅ SLACK_WEBHOOK = " + slackWebhook);

        // Spark 세션 생성
        SparkSession spark = SparkSession.builder()
                .appName("KafkaStructuredStreaming")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        // Kafka 스트림 읽기
        Dataset<Row> kafkaStream = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "sensor-stream")
                .option("startingOffsets", "latest")
                .load();

        // JSON 파싱을 위한 스키마 정의
        StructType schema = new StructType()
                .add("sensor_id", DataTypes.StringType)
                .add("temperature", DataTypes.DoubleType)
                .add("timestamp", DataTypes.LongType);

        // Kafka 메시지 파싱
        Dataset<Row> parsed = kafkaStream
                .selectExpr("CAST(value AS STRING) as json")
                .select(from_json(col("json"), schema).as("data"))
                .select("data.*");

        // 센서별 평균 온도 계산
        Dataset<Row> avgBySensor = parsed
                .groupBy(col("sensor_id"))
                .agg(avg("temperature").alias("avg_temp"));

        // 콘솔 출력 (디버깅 용도)
        avgBySensor.writeStream()
                .format("console")
                .outputMode("update")
                .option("truncate", false)
                .start();

        // MongoDB에 저장 + Slack 알림
        avgBySensor.writeStream()
                .foreach(new ForeachWriter<Row>() {
                    private transient MongoClient mongoClient;

                    @Override
                    public boolean open(long partitionId, long version) {
                        try {
                            System.out.println("🔌 Connecting to MongoDB...");
                            mongoClient = MongoClients.create(mongoUri);
                            return true;
                        } catch (Exception e) {
                            System.out.println("❌ Mongo 연결 실패: " + e.getMessage());
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
                                .insertOne(doc); // ✅ 이 부분이 핵심

                        if (avg > 30.0) {
                            sendSlackAlert(slackWebhook, sensorId, avg);
                        }
                    }

                    @Override
                    public void close(Throwable errorOrNull) {
                        if (mongoClient != null) mongoClient.close();
                    }

                    private void sendSlackAlert(String webhookUrl, String sensorId, double temp) {
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
