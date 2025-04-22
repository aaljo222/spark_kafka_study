$envVars = Get-Content "../.env" | ConvertFrom-StringData
$topic = $envVars.TOPIC_NAME

Write-Host "👀 Kafka Consumer 시작 (토픽: $topic)"
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic $topic --from-beginning
