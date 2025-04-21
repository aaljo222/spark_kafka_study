const express = require("express");
const cors = require("cors");
const { MongoClient } = require("mongodb");
const { Kafka } = require("kafkajs");
require("dotenv").config();

const app = express();
const port = 4000;

app.use(cors());
app.use(express.json()); // ✅ JSON 파싱 필수

const mongoUrl = process.env.MONGO_URI;

// Kafka 설정
const kafka = new Kafka({ brokers: ["kafka:9092"] });
const producer = kafka.producer();

(async () => {
  await producer.connect(); // 서버 시작 시 한 번만 연결
})();

// MongoDB 조회 API
app.get("/api/sensor-avg", async (req, res) => {
  const client = new MongoClient(mongoUrl);
  try {
    await client.connect();
    const data = await client
      .db("sparkdb")
      .collection("sparkdb")
      .find({})
      .sort({ timestamp: -1 })
      .toArray();
    res.json(data);
  } catch (err) {
    console.error("❌ MongoDB 연결 오류:", err);
    res.status(500).json({ error: "MongoDB 연결 오류" });
  } finally {
    await client.close();
  }
});

// Kafka에 센서 전송 API
app.post("/api/send-sensor", async (req, res) => {
  const { sensor_id, temperature } = req.body;

  if (!sensor_id || temperature === undefined) {
    return res.status(400).json({ error: "sensor_id 또는 temperature 누락" });
  }

  try {
    await producer.send({
      topic: "sensor-stream",
      messages: [
        {
          value: JSON.stringify({
            sensor_id,
            temperature,
            timestamp: Date.now(),
          }),
        },
      ],
    });
    res.json({ message: "✅ 센서 데이터 전송됨" });
  } catch (err) {
    console.error("❌ Kafka 전송 실패:", err);
    res.status(500).json({ error: "Kafka 전송 실패" });
  }
});

app.listen(port, () => {
  console.log(`🚀 API server running on http://localhost:${port}`);
});
