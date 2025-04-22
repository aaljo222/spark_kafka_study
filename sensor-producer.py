# =========================================
# 1️⃣ KafkaProducer: sensor-producer.py
# =========================================
from kafka import KafkaProducer
import json, time, random

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

sensor_ids = ["step1", "step2", "step3"]

def generate_event(sensor_id):
    return {
        "sensor_id": sensor_id,
        "temperature": round(random.uniform(25.0, 45.0), 2),
        "timestamp": int(time.time() * 1000)
    }

while True:
    for sid in sensor_ids:
        event = generate_event(sid)
        print(f"Sending: {event}")
        producer.send("sensor-health", value=event)
    time.sleep(2)