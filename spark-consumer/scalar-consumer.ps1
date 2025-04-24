docker run --rm -it `
  --network spark_kafka_study_default `
  -v ${PWD}:/app `
  -w /app `
  bitnami/spark:3.4.1-debian-11-r0 `
  /opt/bitnami/spark/bin/spark-submit `
    --class KafkaToMongoApp `
    --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.4.1,org.mongodb:mongodb-driver-sync:4.11.0 `
    target/scala-2.12/kafkatomongo_2.12-0.1.jar
