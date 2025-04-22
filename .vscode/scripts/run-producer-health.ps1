$envVars = Get-Content "../.env" | ConvertFrom-StringData
$topic = $envVars.TOPIC_NAME

Write-Host "🚀 Kafka Producer 시작 (토픽: $topic)"
docker exec -it kafka kafka-console-producer --broker-list localhost:9092 --topic $topic
