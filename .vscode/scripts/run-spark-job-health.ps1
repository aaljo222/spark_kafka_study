[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "⚙️  Submitting Spark Structured Streaming Job..."

$exitCode = docker exec -it spark /opt/bitnami/spark/bin/spark-submit `
  --master local[*] `
  --class com.example.AppLauncher `
  /app/app.jar spark

if ($LASTEXITCODE -ne 0) {
  Write-Error "❌ Spark job 실행 중 오류가 발생했습니다. 컨테이너 상태를 확인하세요."
  exit 1
}

Write-Host "✅ Spark job 실행 완료"
