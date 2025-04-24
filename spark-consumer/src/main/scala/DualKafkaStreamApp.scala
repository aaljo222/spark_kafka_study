import org.apache.spark.sql.{SparkSession, functions => F}
import org.apache.spark.sql.types._
import sttp.client3.quick._
import sttp.client3.UriContext
import sttp.client3.HttpClientSyncBackend  // 👈 이거 꼭 추가해야 함!

object DualKafkaStreamApp {

  def main(args: Array[String]): Unit = {
    // 환경 변수에서 설정 읽기
    val sensorMongoUri = sys.env("SENSOR_MONGO_URI")
    val eventMongoUri = sys.env("WIDGET_MONGO_URI")
    val kafkaBroker = sys.env("KAFKA_BROKER")
    val sensorTopic = sys.env("SENSOR_TOPIC")
    val eventTopic = sys.env("EVENT_TOPIC")
    val slackWebhook = sys.env("SLACK_WEBHOOK")

    val spark = SparkSession.builder()
      .appName("DualKafkaStreamProcessor")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val sensorSchema = StructType(Seq(
      StructField("sensor_id", StringType),
      StructField("temperature", DoubleType),
      StructField("timestamp", LongType)
    ))

    val eventSchema = StructType(Seq(
      StructField("event_id", StringType),
      StructField("type", StringType),
      StructField("triggered_by", StringType),
      StructField("timestamp", LongType)
    ))

    // Slack 알림 함수
    def sendSlackAlert(sensorId: String, temp: Double): Unit = {
      if (temp > 35.0 && slackWebhook.nonEmpty) {
        val msg = s"""{"text": "\ud83d\udea8 Alert! Sensor $sensorId temperature is $temp\u00b0C"}"""
        quickRequest
          .post(uri"$slackWebhook")
          .body(msg)
          .contentType("application/json")
          .send(HttpClientSyncBackend())
      }
    }

    // sensor-health 처리 및 알림 전송
    val sensorQuery = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBroker)
      .option("subscribe", sensorTopic)
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING) as json")
      .select(F.from_json(F.col("json"), sensorSchema).as("data"))
      .select("data.*")
      .withColumn("alert_flag", F.col("temperature") > 35.0)
      .writeStream
      .foreachBatch { (batchDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row], batchId: Long) =>
        val alerts = batchDF.filter(F.col("temperature") > 35.0).collect()
        alerts.foreach(row => sendSlackAlert(row.getAs[String]("sensor_id"), row.getAs[Double]("temperature")))

        batchDF.write
          .format("mongodb")
          .option("spark.mongodb.write.connection.uri", sensorMongoUri)
          .mode("append")
          .save()
      }
      .option("checkpointLocation", "/tmp/checkpoint-sensor")
      .start()

    // event-topic 저장
    val eventQuery = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBroker)
      .option("subscribe", eventTopic)
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING) as json")
      .select(F.from_json(F.col("json"), eventSchema).as("data"))
      .select("data.*")
      .writeStream
      .format("mongodb")
      .option("spark.mongodb.write.connection.uri", eventMongoUri)
      .option("checkpointLocation", "/tmp/checkpoint-event")
      .outputMode("append")
      .start()

    spark.streams.awaitAnyTermination()
  }
}