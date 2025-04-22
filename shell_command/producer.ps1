# .env 로드
Get-Content .env | ForEach-Object {
  if ($_ -match "^\s*#|^\s*$") { return }
  $key, $value = $_ -split '=', 2
  Set-Item -Path "env:$($key.Trim())" -Value $value.Trim()
}

$TOPIC_NAME = $env:TOPIC_NAME

Write-Host "🚀 Starting Kafka Producer for topic: $TOPIC_NAME"

docker exec -it kafka kafka-console-producer `
  --broker-list localhost:9092 `
  --topic $TOPIC_NAME
