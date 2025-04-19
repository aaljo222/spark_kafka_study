import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import "chart.js/auto";

export default function SensorChart() {
  const [chartData, setChartData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:4000/api/temperatures")
      .then((res) => {
        console.log("res:",res)
        if (!res.ok) throw new Error("데이터 요청 실패");
        return res.json();
      })
      .then((data) => {
        if (data && data.length > 0) {
          setChartData({
            labels: data.map((d) => d.sensor_id),
            datasets: [
              {
                label: "평균 온도 (℃)",
                data: data.map((d) => d.avg_temp),
                backgroundColor: "rgba(75, 192, 192, 0.6)",
                borderWidth: 1,
              },
            ],
          });
        } else {
          setError("데이터가 없습니다.");
        }
      })
      .catch((err) => {
        console.error(err);
        setError("데이터를 불러오는 중 오류가 발생했습니다.");
      });
  }, []);

  if (error) {
    return <div style={{ color: "red" }}>{error}</div>;
  }

  if (!chartData) {
    return <div>📊 데이터를 불러오는 중입니다...</div>;
  }

  return (
    <div style={{ width: "80%", margin: "auto" }}>
      <h2>센서별 평균 온도</h2>
      <Bar
        data={chartData}
        options={{
          responsive: true,
          plugins: {
            legend: {
              position: "top",
            },
          },
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: "온도 (℃)",
              },
            },
            x: {
              title: {
                display: true,
                text: "센서 ID",
              },
            },
          },
        }}
      />
    </div>
  );
}
