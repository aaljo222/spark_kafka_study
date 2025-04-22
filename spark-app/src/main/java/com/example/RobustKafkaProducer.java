package com.example;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.KafkaException;

import java.util.Properties;

public class RobustKafkaProducer {
    private final KafkaProducer<byte[], byte[]> producer;
    private boolean lastSendFailed = false;

    public RobustKafkaProducer(Properties props) {
        System.out.println("🟢 Kafka RobustKafkaProducer  구동 중...");
        this.producer = new KafkaProducer<>(props);
    }

    public boolean sendRecords(ProducerRecord<byte[], byte[]> producerRecord) {
        try {
            final String topic = producerRecord.topic();

            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e != null) {
                        System.err.printf("❌ Failed to send record to %s: %s%n", topic, e.getMessage());
                        e.printStackTrace();
                    } else {
                        System.out.printf("✅ Sent to topic=%s partition=%d offset=%d%n",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    }
                }
            });

            lastSendFailed = false;
        } catch (RetriableException e) {
            System.err.println("⚠️ Retriable exception occurred. Will retry later: " + e.getMessage());
            lastSendFailed = true;
            return false; // 재시도 로직에서 다시 호출할 수 있도록 false 리턴
        } catch (KafkaException e) {
            System.err.println("❌ Fatal Kafka exception: " + e.getMessage());
            throw new RuntimeException("Unrecoverable Kafka error", e);
        }

        return true;
    }

    public void close() {
        producer.close();
    }
}
