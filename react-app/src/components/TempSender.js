import React from "react";

const TempSender = () => {
  console.log("센서 데이터 전송");
  const sensors = [
    { sensor_id: "sensor-101", temperature: 31.5 },
    { sensor_id: "sensor-102", temperature: 28.4 },
    { sensor_id: "sensor-103", temperature: 36.7 },
  ];

  sensors.forEach((sensor) => {
    fetch("http://localhost:4000/api/send-sensor", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(sensor),
    });
  });

  return <div>TempSender</div>;
};

export default TempSender;
