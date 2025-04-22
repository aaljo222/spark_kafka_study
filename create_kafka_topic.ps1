[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$topic = "sensor-stream"
$container = "kafka"

Write-Host "📌 Kafka 컨테이너에서 '$topic' 토픽을 생성합니다..."

docker-compose exec $container kafka-topics --create `
  --topic $topic `
  --bootstrap-server localhost:9092 `
  --partitions 2 `
  --replication-factor 1 `
  --if-not-exists

if ($LASTEXITCODE -ne 0) {
  Write-Error "❌ Kafka 토픽 생성에 실패했습니다. Kafka 컨테이너가 실행 중인지 확인하세요."
  exit 1
}

Write-Host "`n__________________________________"
Write-Host "📋 현재 Kafka 토픽 목록:"
Write-Host "__________________________________`n"


docker-compose exec $container kafka-topics --list --bootstrap-server localhost:9092
