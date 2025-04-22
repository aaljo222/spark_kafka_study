package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ProducerMain {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .filename(".env")
                .load();  // ← 명시적 로드

        String kafkaBroker = dotenv.get("KAFKA_BROKER", "kafka:9092");
        System.out.println("✅ kafkaBroker = " + kafkaBroker);
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBroker);  // ← 여기도 적용

        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        RobustKafkaProducer kafkaSender = new RobustKafkaProducer(props);
        System.out.println("🟢 Kafka Producer 구동 중...");
        for (int i = 0; i < 10; i++) {
            String message = "Test message #" + i;
            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>("sensor-stream", message.getBytes());

            if (!kafkaSender.sendRecords(record)) {
                System.out.println("❌ 메시지 전송 실패. 나중에 재시도할 수 있음.");
            }
        }

        kafkaSender.close();
    }
}
