[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$envVars = Get-Content "../.env" | ConvertFrom-StringData
$topic = $envVars.TOPIC_NAME
$broker = $envVars.KAFKA_BROKER

Write-Host "📌 Kafka 토픽 '$topic' 생성 중..."

docker-compose exec kafka kafka-topics --create `
  --topic $topic `
  --bootstrap-server $broker `
  --partitions 3 `
  --replication-factor 1 `
  --if-not-exists
