// StreamingCombineByKeyApp.java
package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.example.domain.AggregatedResult;
import com.example.domain.SensorInput;
import com.example.domain.SensorState;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.MapGroupsWithStateFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.GroupState;
import org.apache.spark.sql.streaming.GroupStateTimeout;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.bson.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class StreamingCombineByKeyApp {
    public static void main(String[] args) throws StreamingQueryException, TimeoutException {
//        System.setProperty("hadoop.home.dir", "C:\\winutils\\hadoop-3.2.2");
        Dotenv dotenv = Dotenv.load();
        String mongoUri = dotenv.get("MONGO_URI");
        String slackWebhook = dotenv.get("SLACK_WEBHOOK");

        SparkSession spark = SparkSession.builder()
                .appName("KafkaStructuredStreamingCombineByKey")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        StructType schema = new StructType()
                .add("sensor_id", DataTypes.StringType)
                .add("temperature", DataTypes.DoubleType)
                .add("timestamp", DataTypes.LongType);

        Dataset<Row> kafkaStream = spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "kafka:9092")
                .option("subscribe", "sensor-stream")
                .option("startingOffsets", "latest")
                .load();

        Dataset<SensorInput> parsed = kafkaStream
                .selectExpr("CAST(value AS STRING) as json")
                .select(from_json(col("json"), schema).as("data"))
                .select("data.*")
                .as(Encoders.bean(SensorInput.class));

        Dataset<AggregatedResult> aggregated = parsed
                .groupByKey(
                        (MapFunction<SensorInput, String>) SensorInput::getSensor_id,
                        Encoders.STRING()
                )
                .mapGroupsWithState(
                        new MapGroupsWithStateFunction<String, SensorInput, SensorState, AggregatedResult>() {
                            @Override
                            public AggregatedResult call(String sensorId, Iterator<SensorInput> values, GroupState<SensorState> state) {
                                SensorState currentState = state.exists() ? state.get() : new SensorState(0.0, 0);
                                while (values.hasNext()) {
                                    SensorInput input = values.next();
                                    currentState.sum += input.getTemperature();
                                    currentState.count += 1;
                                }
                                state.update(currentState);
                                return new AggregatedResult(sensorId, currentState.avg());
                            }
                        },
                        Encoders.bean(SensorState.class),        // state encoder
                        Encoders.bean(AggregatedResult.class),   // result encoder
                        GroupStateTimeout.NoTimeout()            // timeout
                );



        StreamingQuery query = aggregated.writeStream()
                .foreach(new ForeachWriter<AggregatedResult>() {
                    private transient MongoClient mongoClient;
                    private transient MongoCollection<Document> collection;

                    @Override
                    public boolean open(long partitionId, long version) {
                        mongoClient = MongoClients.create(mongoUri);
                        MongoDatabase db = mongoClient.getDatabase("sparkdb");
                        collection = db.getCollection("sparkdb");
                        return true;
                    }

                    @Override
                    public void process(AggregatedResult result) {
                        System.out.printf("\uD83D\uDCC5 Mongo Insert: %s, avg: %.2f°C%n", result.getSensor_id(), result.getAvg_temp());
                        Document doc = new Document("sensor_id", result.getSensor_id())
                                .append("avg_temp", result.getAvg_temp())
                                .append("timestamp", new Date());
                        collection.insertOne(doc);

                        if (result.getAvg_temp() > 30.0) {
                            sendSlack(slackWebhook, result.getSensor_id(), result.getAvg_temp());
                        }
                    }

                    @Override
                    public void close(Throwable errorOrNull) {
                        mongoClient.close();
                    }

                    private void sendSlack(String webhookUrl, String sensorId, double avgTemp) {
                        String msg = String.format("\uD83D\uDD25 경고! %s 센서 온도 %.2f°C", sensorId, avgTemp);
                        try {
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(webhookUrl))
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + msg + "\"}"))
                                    .build();
                            client.send(req, HttpResponse.BodyHandlers.ofString());
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .outputMode("update")
                .start();

        query.awaitTermination();
    }
}