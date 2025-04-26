package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryConsumer {

    private final MongoTemplate mongoTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${SLACK_WEB_HOOK}")
    private String slackWebhookUrl;

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void consume(String order) {
        log.info("📥 Received Order: {}", order);

        try {
            // 1) Kafka 메시지에서 양쪽 큰따옴표 제거
            String cleanOrder = order.replaceAll("^\"|\"$", "");

            // 2) MongoDB에 저장
            OrderEntity orderEntity = new OrderEntity(cleanOrder);
            mongoTemplate.save(orderEntity);
            log.info("✅ MongoDB 저장 성공: {}", cleanOrder);

            // 3) 슬랙 알림 전송 (Map 자동 직렬화)
            String slackText = "New order received: " + cleanOrder;
            Map<String, String> payload = Collections.singletonMap("text", slackText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(slackWebhookUrl, entity, String.class);
            log.info("📡 Slack 알림 전송 성공");

        } catch (Exception e) {
            log.error("❌ 처리 중 에러 발생", e);
        }
    }
}
