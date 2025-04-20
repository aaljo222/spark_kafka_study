const express = require("express");
const cors = require("cors");
const { MongoClient } = require("mongodb");
require("dotenv").config(); // .env 로드

const app = express();
const port = 4000;

app.use(cors());

const mongoUrl = process.env.MONGO_URI;

app.get("/api/sensor-avg", async (req, res) => {
  const client = new MongoClient(mongoUrl);
  try {
    await client.connect();

    const data = await client
      .db("sparkdb")
      .collection("sparkdb")
      .find({})
      .sort({ timestamp: -1 })
      .limit(10)
      .toArray();

    res.json(data);
  } catch (err) {
    console.error("❌ MongoDB 연결 오류:", err);
    res.status(500).json({ error: "MongoDB 연결 오류" });
  } finally {
    await client.close();
  }
});

app.listen(port, () => {
  console.log(`🚀 API server running on http://localhost:${port}`);
});
