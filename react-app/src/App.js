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
import TempSender from "./components/TempSender";

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
      <TempSender />
      <h2>Sensor Temperature Chart</h2>
      <LineChart width={600} height={300} data={data}>
        <XAxis
          dataKey="timestamp"
          tickFormatter={(ts) => new Date(ts).toLocaleTimeString()}
        />
        <YAxis domain={["auto", "auto"]} />
        <Tooltip />
        <CartesianGrid stroke="#ccc" />
        <Line type="monotone" dataKey="avg_temp" stroke="#8884d8" />
      </LineChart>

      <h3>📊 센서별 평균 온도</h3>
      <ul>
        {data.map((item, index) => (
          <li key={index}>
            센서: {item.sensor_id}, 평균 온도:
            {typeof item.avg_temp === "number"
              ? item.avg_temp.toFixed(2)
              : "N/A"}
            °C
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
