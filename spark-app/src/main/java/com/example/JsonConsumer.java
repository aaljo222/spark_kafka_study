package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.kafka.clients.consumer.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class JsonConsumer {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("spark-app") // 또는 절대경로
                .load();

        System.out.println("📦 .env 환경변수 디버깅 -------------------");
        // 주요 변수 출력
        System.out.println("✅ KAFKA_BROKER:   " + dotenv.get("KAFKA_BROKER"));
        System.out.println("✅ MONGO_URI:      " + dotenv.get("MONGO_URI"));
        System.out.println("✅ SLACK_WEBHOOK:  " + dotenv.get("SLACK_WEBHOOK"));


        System.out.println("--------------------------------------------");
        String kafkaBroker = dotenv.get("KAFKA_BROKER", "kafka:9092");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);  // ← 적용

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "json-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("sensor-stream"));
        System.out.println("🟢 Kafka Consumer 구동 중...");

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("📥 JSON 메시지 수신: " + record.value());
            }
        }
    }
}
