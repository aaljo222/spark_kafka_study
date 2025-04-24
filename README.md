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

## 📊 Kafka 관련 아키텍처 & 구조 이미지 모음

### ✅ Kafka 기본 구조

![Kafka 구조1](./images/kafka_1.PNG)  
![Kafka 구조2](./images/kafka_2.PNG)  
![Kafka 구조3](./images/kafka_3.PNG)  
![Kafka 구조4](./images/kafka_4.PNG)  
![Kafka 구조5](./images/kafka_5.PNG)  

---

### 📌 Kafka 활용 사례 & 사용 흐름

![Kafka Use Case](./images/kafka_use_case.jpg)  
![Kafka Workflow](./images/kafka_workflow.gif)  
![Kafka Workflow 2](./images/kafka_workflow2.gif)  
![Kafka Workflow 3](./images/kafka_workflow3.gif)  
![Kafka Workflow 4](./images/kafka_workflow4.gif)  
![Kafka Workflow 5](./images/kafka_workflow5.gif)  
![Kafka Workflow 6](./images/kafka_workflow6.gif)  
![Kafka Workflow 7](./images/kafka_workflow7.gif)  

---

### 🔁 Pub/Sub & Microservice Architecture

![Pub/Sub 모델](./images/kafka_workflow6.gif)  
![Pub/Sub 패턴](./images/kafka_workflow7.gif)  
![Microservice 구조](./images/micro_service_architecture.gif)

---

### ☸️ Kafka on Kubernetes

![Kafka on K8s](./images/k8s_cluster.gif)  
![Kubernetes 구조](./images/kubernetes.jpg)


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

## producer 
docker exec -it kafka kafka-console-producer --broker-list localhost:9092 --topic sensor-stream

{"sensor_id":"sensor_1","temperature":24.5,"timestamp":1713763500}

## consumer
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic sensor-stream --from-beginning

## 🔄 Kafka 구조 설명

Kafka는 **고성능 분산 메시징 시스템**으로, 아래와 같은 구조로 구성됩니다.

![Kafka 구조](./images/kafka-architecture-diagram.PNG)
![Kafka 구조](./images/kafka-architecture-diagram2.PNG)

---

### 📤 Producer Clients (생산자 클라이언트)

Kafka에 데이터를 전송하는 시스템입니다. 예시로는:

- **Databases**: 변경 이벤트나 트리거 데이터를 전송
- **IoT events**: 센서 데이터 등 실시간 스트리밍
- **Web events**: 사용자 클릭, 페이지 뷰 등 이벤트
- **Logs**: 서버나 애플리케이션 로그 수집

이들은 Kafka의 **topic**에 데이터를 보냅니다.

---

### 📦 Kafka Core (메시지 브로커)

Kafka의 핵심 처리 영역입니다.

- **OS page cache (memory)**: 먼저 메모리에 기록된 후
- **Flushed to disk**: 디스크로 저장됩니다 (성능 + 내구성)
- **Topic**: 데이터 저장 단위 (토픽은 여러 **Partition**으로 구성됨)
- **Partition 구조**:
  - 각 파티션은 Append-only 로그
  - 각 메시지는 고유한 **offset**으로 식별됨
  - 메시지는 오래 보관되며, 언제든 재처리 가능

```text
Topic: sensor-stream
Partition 0:  [0] [1] [2] [3] [4] → 계속 추가됨

# Apache Kafka 실행 스크립트 및 설정 파일 설명

이 디렉토리는 Apache Kafka를 운영하거나 테스트하는 데 사용되는 다양한 실행 스크립트와 프로퍼티 설정 파일들을 포함하고 있습니다.

---

## 🧩 주요 실행 스크립트 (`*.sh`)

| 파일명 | 설명 |
|--------|------|
| kafka-server-start.sh | Kafka 브로커를 시작하는 스크립트 |
| kafka-server-stop.sh | Kafka 브로커를 종료하는 스크립트 |
| zookeeper-server-start.sh | Zookeeper 서버 시작 |
| zookeeper-server-stop.sh | Zookeeper 서버 종료 |
| kafka-topics.sh | 토픽 생성, 삭제, 조회 등 관리 |
| kafka-console-producer.sh | 콘솔에서 메시지를 Kafka에 전송 |
| kafka-console-consumer.sh | 콘솔에서 Kafka 메시지를 소비 |
| kafka-consumer-groups.sh | 소비자 그룹 조회 및 오프셋 관리 |
| kafka-configs.sh | 토픽, 사용자, 브로커 설정 변경 |
| kafka-acls.sh | ACL(접근 제어 목록) 관리 |
| kafka-streams-application-reset.sh | Kafka Streams 애플리케이션 초기화 |
| kafka-delete-records.sh | 특정 파티션의 레코드 삭제 |
| kafka-log-dirs.sh | 브로커의 로그 디렉토리 조회 |
| kafka-dump-log.sh | 로그 세그먼트 파일을 덤프 |
| kafka-reassign-partitions.sh | 파티션 재할당 수행 |
| kafka-preferred-replica-election.sh | 선호 리더 복구 프로세스 수행 |
| kafka-replica-verification.sh | 복제 상태 검증 |
| kafka-mirror-maker.sh | Kafka 간 데이터 복제 도구 |
| kafka-producer-perf-test.sh | Kafka 생산자 성능 테스트 |
| kafka-consumer-perf-test.sh | Kafka 소비자 성능 테스트 |
| kafka-broker-api-versions.sh | 브로커가 지원하는 API 버전 확인 |
| kafka-run-class.sh | Kafka 관련 클래스 실행용 도구 |
| trogdor.sh | Kafka 스트레스 테스트 도구 실행 |

---

## 🔌 Kafka Connect 관련 스크립트

| 파일명 | 설명 |
|--------|------|
| connect-standalone.sh | 단일 프로세스로 Connect 실행 |
| connect-distributed.sh | 분산형 Connect 클러스터 실행 |
| connect-console-source.properties | 콘솔 입력을 소스 데이터로 사용하는 설정 |
| connect-console-sink.properties | 콘솔 출력으로 데이터를 보내는 설정 |
| connect-file-source.properties | 파일을 읽어 Kafka로 보내는 소스 커넥터 설정 |
| connect-file-sink.properties | Kafka 메시지를 파일로 저장하는 싱크 커넥터 설정 |

---

## ⚙️ 설정 파일들 (`*.properties`)

| 파일명 | 설명 |
|--------|------|
| server.properties | Kafka 브로커 설정 (포트, 로그 경로 등) |
| zookeeper.properties | Zookeeper 설정 파일 |
| producer.properties | Kafka 프로듀서 설정 |
| consumer.properties | Kafka 컨슈머 설정 |
| log4j.properties | Kafka 로그 설정 파일 |
| connect-log4j.properties | Connect 로그 설정 |
| tools-log4j.properties | Kafka 유틸리티 로그 설정 |
| connect-standalone.properties | Connect Standalone 모드 기본 설정 |
| connect-distributed.properties | Connect Distributed 모드 기본 설정 |
| trogdor.conf | Trogdor 성능 테스트 도구 설정 |

---

## 🗂 기타

| 디렉토리 | 설명 |
|----------|------|
| windows | Windows 환경용 실행 스크립트 보관 디렉토리 |

---

## ✅ 참고

- 대부분의 `*.sh` 스크립트는 `$KAFKA_HOME/bin` 경로에서 실행
- Kafka 실행 전 Zookeeper부터 먼저 실행 필요


