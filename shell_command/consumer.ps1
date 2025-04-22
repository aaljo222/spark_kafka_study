# .env 환경 변수 불러오기
Get-Content .env | ForEach-Object {
  if ($_ -match "^\s*#|^\s*$") { return }
  $key, $value = $_ -split '=', 2
  Set-Item -Path "env:$($key.Trim())" -Value $value.Trim()
}

$TOPIC_NAME = $env:TOPIC_NAME

Write-Host "👀 Starting Kafka Consumer for topic: $TOPIC_NAME"

docker exec -it kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic $TOPIC_NAME `
  --from-beginning
