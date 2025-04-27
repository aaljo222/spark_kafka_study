# 📦 Kafka Widget Streaming App

This project demonstrates real-time widget order ingestion via **Apache Kafka**, processing in **Apache Spark**, and visualization through **Akka HTTP API** with storage in **MongoDB**.

---

## 🚀 Quick Start with Docker Compose

```bash
docker-compose up --build
```

This will start the following services:

- **Zookeeper** (2181)
- **Kafka** (9092 internal, 29092 for Kafka clients)
- **MongoDB** (27017)
- **Spark** (runs `KafkaToMongoApp` with `spark-submit`)
- **Widget API** (`WidgetServiceAPI` exposed on `localhost:8080`)
- **Kafdrop UI** (Kafka web UI on [localhost:9000](http://localhost:9000))

---

## 🔧 API Access & Testing

### 1. Exposed Port

Ensure your Docker container exposes the service port:

```dockerfile
EXPOSE 8080
```

And in `docker-compose.yml`:

```yaml
ports:
  - "8080:8080"
```

### 2. Internal Container Test

To test API access from inside the container:

```bash
docker exec -it widget-service bash
curl http://localhost:8080/orders/Fred
```

### 3. Windows Host Access (Outside Docker)

On Windows, use the Docker host gateway:

```powershell
Invoke-RestMethod -Uri http://host.docker.internal:8080/orders/Fred
```

---

## 🧪 Example Kafka Test Message

Send a Kafka event to `event-topic`:

```bash
echo '{ "type":"WidgetOrdered", "customerId":"Fred", "widgetId":"W123" }' | docker exec -i kafka /usr/bin/kafka-console-producer   --broker-list kafka:29092 --topic event-topic
```

Then query with:

```bash
Invoke-RestMethod -Uri http://host.docker.internal:8080/orders/Fred
```

You should see the widget order response if everything is working ✅

---

## 📂 Folder Structure

```
spark-consumer/
├── Dockerfile
├── build.sbt
├── src/main/scala/
│   ├── model/
│   │   ├── Order.scala
│   │   └── JsonProtocol.scala
│   ├── WidgetServiceAPI.scala
│   └── WidgetEventConsumer.scala
```

---

Happy Streaming! ⚡

✅ 해결 방법 (Windows용 Spark 필수 설정)

1. winutils.exe 다운로드
   GitHub에서 Spark 버전과 호환되는 winutils.exe 다운로드:
   👉 https://github.com/steveloughran/winutils

예시:

Spark 3.4.x → Hadoop 3.0.0 winutils 필요

예: C:\winutils\hadoop-3.0.0\bin\winutils.exe 위치에 저장 2. 환경변수 설정
PowerShell 또는 cmd에서:
$env:HADOOP_HOME="C:\winutils\hadoop-3.0.0"
$env:PATH += ";$env:HADOOP_HOME\bin"

### docker에서의  mongodb를  local에서 접속하려면 
mongodb://host.docker.internal:27017
  

docker exec -it kafka /usr/bin/kafka-console-consumer `
  --bootstrap-server kafka:29092 `
  --topic sensor-health `
  --from-beginning

## 🔄 이벤트 기반 아키텍처 (Event-driven Architecture)

![event-driven](./images/event_driven.png)


# 리눅스 커널 → Container Runtime → Kubernetes → Kafka 이해 흐름도

## 📦 리눅스 커널 레벨
```
[ 리눅스 커널 ]
├─ PID Namespace  →  프로세스 격리
├─ Cgroup         →  CPU/Memory 자원 제한
├─ NET Namespace  →  네트워크 격리
├─ Filesystem     →  디스크 및 스토리지 관리
├─ TCP/IP Stack   →  네트워크 통신 처리
├─ Scheduler      →  CPU 스케줄링
```

↓

## 🚀 Container Runtime (containerd, CRI-O)
```
[ Container Runtime ]
├─ API 요청 수신 (Kubelet → containerd)
├─ runC 호출해서 컨테이너 프로세스 생성 (fork → clone → setns → exec)
├─ PID Namespace, NET Namespace 생성
├─ Cgroup 설정해서 CPU/Memory 제한 적용
├─ OverlayFS로 파일 시스템 레이어 구성
├─ 네트워크 네임스페이스 설정 후 가상 네트워크 연결 (veth, bridge)
```

↓

## 🚀 Kubernetes (Pod, Node, Service)
```
[ Kubernetes ]
├─ API Server가 Kubelet에 Pod 생성 요청
├─ Kubelet이 Container Runtime(containerd) 호출
├─ Pod마다 독립된 PID/NET Namespace, Cgroup 설정
├─ kube-proxy가 TCP/UDP 트래픽 라우팅
├─ Service/Ingress 통해 Pod 간 통신 라우팅
├─ kube-scheduler가 클러스터 자원 기반으로 Pod 배치
├─ Node Controller, ReplicaSet Controller로 상태 모니터링 및 조정
```

↓

## 🚀 Kafka (Broker, Zookeeper, Producer/Consumer)
```
[ Kafka Ecosystem ]
├─ Broker는 독립된 OS 프로세스 (컨테이너 or VM 내)
├─ TCP 소켓으로 Producer ↔ Broker ↔ Consumer 통신
├─ Broker 간 Replication 통신 (TCP/IP 기반, ISR 구조)
├─ 로그 세그먼트 파일은 Filesystem 상에 저장 및 관리 (Write-Ahead Logging)
├─ ZooKeeper(또는 KRaft)로 클러스터 메타데이터 관리
├─ Broker는 Topic/Partition 스케줄링, 데이터 복제, 리더 선출 등을 담당
```

---

# 🔥 최종 요약
> 리눅스 커널의 자원 격리/관리 메커니즘 → 컨테이너 런타임(runC, containerd) → Kubernetes 조정(API Server, Scheduler, Controller) → Kafka 분산 메시징 시스템 구축
>
> 이 흐름을 이해하면, 현대 클라우드 네이티브 인프라(K8s + Kafka)의 작동 원리와 최적화 포인트를 커널 수준에서 파악할 수 있다.

---

# 📄 참고용 README.md 작성 방향

## 📚 프로젝트 목표
- 리눅스 커널 구조와 기능 이해 (Namespace, Cgroup, Filesystem, Scheduler)
- 컨테이너 런타임(Containerd, runC) 작동 방식 분석
- Kubernetes 클러스터 내부 컴포넌트 흐름 분석
- Kafka 분산 메시징 시스템 아키텍처 이해

## 🏗️ 분석 흐름
1. 리눅스 커널 기능별 분석 (Namespace, Cgroup, Filesystem)
2. Container Runtime에서의 시스템콜 동작(fork, clone, setns)
3. Kubernetes Pod 생성/관리 흐름
4. Kafka의 Broker 관리, 데이터 복제, 통신 구조 분석

## 🔥 기대 효과
- Kubernetes 및 Kafka 시스템을 최적화할 때 리눅스 커널 레벨에서 문제를 진단 가능
- 장애 발생 시 커널 → 런타임 → 쿠버네티스 → 애플리케이션까지 풀스택으로 분석 가능

