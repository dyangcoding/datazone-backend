name := "data-zone"
version := "0.1"
scalaVersion := "2.13.5"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
val MongoReactive = "1.0.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

  // Http Library
  "org.scalaj" %% "scalaj-http" % "2.4.2",

  // Reactive Mongo
  "org.reactivemongo" %% "reactivemongo" % MongoReactive,
)