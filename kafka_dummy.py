from kafka import KafkaConsumer
from pymongo import MongoClient
import json

# MongoDB 연결
mongo_client = MongoClient("mongodb+srv://greenuser:kEMVzwlRbCIjQcta@cluster0.tm3m3ep.mongodb.net/sparkdb?retryWrites=true&w=majority")
db = mongo_client["sparkdb"]
collection = db["sparkdb"]

# Kafka Consumer 설정
consumer = KafkaConsumer(
    "sensor-stream",
    bootstrap_servers='localhost:9092',
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    auto_offset_reset='earliest',
    enable_auto_commit=True,
    group_id="mongo-group"
)

# Kafka 메시지 30개 저장
count = 0
for message in consumer:
    print(f"📥 Received {count+1}/30:", message.value)
    collection.insert_one(message.value)
    count += 1
    if count >= 30:
        print("✅ 30개 메시지 저장 완료.")
        break
