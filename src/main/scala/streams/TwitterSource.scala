package streams

import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{DataFrame, SparkSession}
import tweets.TwitterConnectionImpl

object TwitterSource {
  private val SOURCE_PROVIDER_CLASS = TwitterStreamingSource.getClass.getCanonicalName

  def main(args: Array[String]): Unit = {
    println("DataZone")

    val providerClassName = SOURCE_PROVIDER_CLASS.substring(0, SOURCE_PROVIDER_CLASS.indexOf("$"))
    println(providerClassName)

    val spark = SparkSession
      .builder
      .appName("DataZone")
      .master("local[*]")
      .getOrCreate()

    val tweetDF: DataFrame = spark
      .readStream
      .format(providerClassName)
      .option(TwitterStreamingSource.QUEUE_SIZE, 10000)
      .load

    tweetDF.printSchema

    val x: StreamingQuery =tweetDF.writeStream.foreach(new MongoForEachWriter)
      .outputMode("append")
      .start()

    while(true){}

    println("*******************************************************************************************")
    println("Spark Thread stopped")
    println("*******************************************************************************************")
    x.stop
    TwitterConnectionImpl.stop
    spark.stop
  }
}