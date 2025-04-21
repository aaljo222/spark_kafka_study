# run_all.ps1
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Step 1: docker-compose up -d (백그라운드 실행)
Write-Host "🚀 Docker Compose 서비스 시작..."
docker-compose up -d

# Step 2: Kafka 준비 대기 (Zookeeper/Kafka가 완전히 뜨는 데 몇 초 걸림)
Start-Sleep -Seconds 10

# Step 3: Kafka 토픽 생성
Write-Host "📌 Kafka 토픽 생성 스크립트 실행..."
.\create_kafka_topic.ps1
