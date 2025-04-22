[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "⚙️  Submitting Spark Structured Streaming Job..."

docker exec -it spark /opt/bitnami/spark/bin/spark-submit `
  --master local[*] `
  --class com.example.AppLauncher `
  /app/app.jar spark
