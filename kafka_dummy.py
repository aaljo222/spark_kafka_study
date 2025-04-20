from kafka import KafkaProducer
import json
import random
import time

# Kafka Producer 설정
producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)


# 센서 ID 리스트
sensor_ids = [f"sensor-{i:03d}" for i in range(1, 21)]  # sensor-001 ~ sensor-020

# 200개 메시지 전송
for i in range(200):
    data = {
        "sensor_id": random.choice(sensor_ids),
        "temperature": round(random.uniform(20.0, 40.0), 2),
        "timestamp": int(time.time() * 1000)
    }
    producer.send("sensor-stream", value=data)
    print(f"📤 Sent {i+1}/200:", data)
    time.sleep(0.1)  # 전송 간격 (0.1초)
