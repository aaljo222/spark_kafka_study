import React, { useEffect, useState } from "react";
import axios from "axios";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from "recharts";

function App() {
  const [data, setData] = useState([]);

  useEffect(() => {
    axios
      .get("http://localhost:4000/api/sensor-avg")
      .then((res) => {
        console.log("📦 데이터 확인:", res.data); // ← 여기 추가
        setData(res.data);
      })
      .catch((err) => console.error("❌ 요청 실패:", err));
  }, []);

  return (
    <div>
      <h2>Sensor Temperature Chart</h2>
      <LineChart width={600} height={300} data={data}>
        <XAxis dataKey="timestamp" />
        <YAxis domain={["auto", "auto"]} />
        <Tooltip />
        <CartesianGrid stroke="#ccc" />
        <Line type="monotone" dataKey="avg_temp" stroke="#8884d8" />
      </LineChart>
    </div>
  );
}

export default App;
