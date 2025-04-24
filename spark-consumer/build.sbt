name := "KafkaWidgetProject"

version := "0.1"

scalaVersion := "2.12.18"

Compile / mainClass := Some("MongoDBKafka")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "3.4.1",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.4.1",
  "org.mongodb" % "mongodb-driver-sync" % "4.11.0",
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "io.spray" %% "spray-json" % "1.3.6",
  "org.mongodb.spark" %% "mongo-spark-connector" % "10.2.0",
  "com.softwaremill.sttp.client3" %% "core" % "3.9.0",
"com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % "3.9.0"

)

dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
