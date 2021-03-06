name := "data-zone"
version := "0.1"
scalaVersion := "2.12.13"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
val SparkVersion = "2.4.7"
val MongoReactive = "1.0.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

  // Spark Dependencies
  "org.apache.spark" %% "spark-core" % SparkVersion,
  "org.apache.spark" %% "spark-streaming" % SparkVersion,
  "org.apache.spark" %% "spark-sql" % SparkVersion,
  "org.apache.spark" %% "spark-catalyst" % SparkVersion,

  // mongo drive
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",

  // Http Library
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  // CORS Support
  "ch.megard" %% "akka-http-cors" % "1.1.1",

  // Reactive Mongo
  "org.reactivemongo" %% "reactivemongo" % MongoReactive,

  // logger
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

  // tests
  "org.scalactic" %% "scalactic" % "3.2.5",
  "org.scalatest" %% "scalatest" % "3.2.5",
  "org.mockito" %% "mockito-scala" % "1.16.37" % "test"
)