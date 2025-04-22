# =========================================
# 2️⃣ SparkConsumer: Streaming to MongoDB
# =========================================
# Requires spark-submit with Mongo and Kafka packages
# e.g., spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.4.1,org.mongodb.spark:mongo-spark-connector_2.12:10.2.0 app.py

from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col
from pyspark.sql.types import StructType, StringType, DoubleType, LongType
from pymongo import MongoClient
import json
import requests

spark = SparkSession.builder \
    .appName("KafkaToMongo") \
    .getOrCreate()

schema = StructType() \
    .add("sensor_id", StringType()) \
    .add("temperature", DoubleType()) \
    .add("timestamp", LongType())

df = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "kafka:9092") \
    .option("subscribe", "sensor-health") \
    .load()

parsed = df.selectExpr("CAST(value AS STRING)") \
    .select(from_json(col("value"), schema).alias("data")) \
    .select("data.*")

# === ForeachBatch logic ===
def process_batch(batch_df, epoch_id):
    client = MongoClient("mongodb://localhost:27017")
    db = client["sensorDB"]
    collection = db["readings"]

    for row in batch_df.collect():
        doc = row.asDict()
        collection.insert_one(doc)

        # Slack Alert if temperature > 35
        if doc['temperature'] > 35:
            msg = f"\u26a0\ufe0f Alert! {doc['sensor_id']} is too hot ({doc['temperature']} °C)"
            requests.post("https://hooks.slack.com/services/your/slack/webhook", json={"text": msg})

parsed.writeStream \
    .foreachBatch(process_batch) \
    .outputMode("append") \
    .start() \
    .awaitTermination()
