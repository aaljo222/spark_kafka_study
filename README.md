# Kafka + Spark + MongoDB 실시간 스트리밍 예제

## 📦 Kafka 토픽 생성

```
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 2 \
  --topic sensor-stream
```

---

## ⚙️ Spark에서 Kafka 읽기

```java
Dataset<Row> kafkaStream = spark
    .readStream()
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "sensor-stream")
    .option("startingOffsets", "latest")
    .load();
```

> 💡 로컬 개발 시에는 아래와 같이 Hadoop 설정이 필요합니다:

```java
System.setProperty("hadoop.home.dir", "C:\\winutils\\hadoop-3.2.2\\bin");
```

---

## 🐍 Python용 Kafka 테스트

```
pip install kafka-python
```

---

## 🧪 Kafka 컨테이너 접속 및 토픽 생성

```
docker exec -it kafka bash

/usr/bin/kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic sensor-stream \
  --partitions 2 \
  --replication-factor 1
```

---

docker exec -it kafka bash
kafka-console-producer --broker-list localhost:9092 --topic sensor-stream


## 🖼 시스템 아키텍처

### ✅ 데이터 흐름 개요

![FLOW1](./FLOW1.png)

---

### ✅ 전체 흐름도

![FLOW2](./FLOW2.png)

---

### ✅ MQTT 연동 구조 (선택적 확장)

![MQTT](./MQTT.jpg)

## 🧠 Kafka 구조 이미지 모음

Kafka 구조와 스트리밍 흐름을 이해하는 데 도움이 되는 시각 자료들입니다.

### 📌 Kafka 플랫폼 전체 구조
![Kafka 플랫폼](./images/kafka_1.PNG)

### 📌 Kafka Producer → Broker → Consumer 흐름도
![Kafka 구조2](./images/kafka_2.PNG)

### 📌 Kafka 메시지 전달 방식
![Kafka 구조3](./images/kafka_3.PNG)

### 📌 Kafka를 통한 AI 분석 흐름
![Kafka 구조4](./images/kafka_4.PNG)

### 🌀 Kafka 실시간 처리 Workflow (GIF)
![Kafka Workflow](./images/kafka_workflow.gif)

## 🔍 Kafka 구조 시각화 (애니메이션)

Kafka의 내부 구조와 메시지 흐름을 이해하려면 아래 링크들을 참고하세요.

1. 👉 [Kafka Visualisation (SoftwareMill)](https://softwaremill.com/kafka-visualisation/)  
   Kafka의 핵심 개념을 애니메이션으로 보여주는 인터랙티브한 사이트입니다.

2. 👉 [Apache Kafka: An Animated Introduction (Medium by Mark Haynes)](https://medium.com/@mark-haynes/apache-kafka-an-animated-introduction-a553ca57a8a1)  
   Kafka 토픽, 파티션, 컨슈머 그룹 등의 개념을 시각적으로 설명하는 블로그입니다.

---

## 🖼️ Kafka 구성도 (정적 이미지)

프로젝트의 Kafka 구성은 아래와 같습니다:

![Kafka 구조도](./images/kafka_1.PNG)