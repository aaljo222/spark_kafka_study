# filename: run_kafka_spark.ps1

$topicName = "sensor-stream"
$kafkaContainer = "kafka"
$sparkContainer = "spark"

Write-Host "🔍 Kafka 토픽 확인 중: $topicName"

# 토픽 존재 여부 확인
$topicCheck = docker exec $kafkaContainer bash -c "/usr/bin/kafka-topics --bootstrap-server kafka:9092 --list" | Select-String $topicName

if (-not $topicCheck) {
    Write-Host "❌ 토픽이 존재하지 않습니다. 생성 중..."
    docker exec $kafkaContainer bash -c "/usr/bin/kafka-topics --bootstrap-server kafka:9092 --create --topic $topicName --partitions 1 --replication-factor 1"
    Start-Sleep -Seconds 5
} else {
    Write-Host "✅ 토픽이 이미 존재합니다: $topicName"
}

Write-Host "⏳ Spark 컨테이너 재시작 및 스트리밍 앱 실행"
docker restart $sparkContainer
