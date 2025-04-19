$topic = "sensor-stream"
$container = "kafka"

Write-Host "📌 Kafka 컨테이너에서 '$topic' 토픽을 생성합니다..."

# 토픽 생성 명령
docker-compose exec $container kafka-topics --create `
  --topic $topic `
  --bootstrap-server localhost:9092 `
  --partitions 1 `
  --replication-factor 1 `
  --if-not-exists

# 토픽 목록 확인
Write-Host "`n📋 현재 Kafka 토픽 목록:"
docker-compose exec $container kafka-topics --list --bootstrap-server localhost:9092
