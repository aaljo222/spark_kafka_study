import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.types._
import sttp.client3.quick._
import sttp.client3.UriContext
import sttp.client3.HttpClientSyncBackend

object MongoDBKafka {

  // Slack 알림 함수 (직렬화 대상 아님)
  def sendSlackAlert(sensorId: String, temp: Double, webhook: String): Unit = {
    if (temp > 35.0 && webhook.nonEmpty) {
      val backend = HttpClientSyncBackend()
      val msg = s"""{"text": "🚨 Alert! $sensorId temperature is $temp°C"}"""
      quickRequest
        .post(uri"$webhook")
        .body(msg)
        .contentType("application/json")
        .send(backend)
      backend.close()
    }
  }

  def main(args: Array[String]): Unit = {
    val kafkaBroker = sys.env.getOrElse("KAFKA_BROKER", "localhost:9092")
    val topicName = sys.env.getOrElse("TOPIC_NAME", "sensor-health")
    val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://localhost:27017/sensorDB.readings")
    val slackWebhook = sys.env.getOrElse("SLACK_WEBHOOK", "")

    val spark = SparkSession.builder()
      .appName("KafkaToMongoDB")
      .master("local[*]")
      .config("spark.mongodb.write.connection.uri", mongoUri)
      .getOrCreate()

    import spark.implicits._

    val schema = StructType(Seq(
      StructField("sensor_id", StringType),
      StructField("temperature", DoubleType),
      StructField("timestamp", LongType)
    ))

    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBroker)
      .option("subscribePattern", "sensor-health|event-topic")
      .option("startingOffsets", "latest")
      .load()

    val parsed = kafkaDF.selectExpr("CAST(value AS STRING) as json")
      .select(F.from_json(F.col("json"), schema).as("data"))
      .select("data.*")

    // ❌ Slack 알림은 여기서 하지 않음. 대신 나중에 후처리
    val withAlertFlag = parsed.withColumn(
      "alert_flag",
      F.when(F.col("temperature") > 35.0, F.lit(true)).otherwise(F.lit(false))
    )

    val query = withAlertFlag.writeStream
      .format("mongodb")
      .option("checkpointLocation", "/tmp/kafka-to-mongo-checkpoint")
      .outputMode("append")
      .start()

    println("✅ Streaming started. Slack alerts should be triggered externally based on MongoDB data.")
    query.awaitTermination()
  }
}
