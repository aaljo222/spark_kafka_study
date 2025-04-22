import React from "react";

const TempSender = () => {
  console.log("센서 데이터 전송");
  const sensors = [
    { sensor_id: "sensor11-1101", temperature: 231.5 },
    { sensor_id: "sensor11-1102", temperature: 28.4 },
    { sensor_id: "sensor11-1103", temperature: 236.7 },
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
