package streams

import com.mongodb.client.MongoCollection
import com.mongodb.spark.MongoConnector
import com.mongodb.spark.config.WriteConfig
import org.apache.spark.sql.functions.{current_timestamp, from_json, from_unixtime, unix_timestamp, window}
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, ForeachWriter, SparkSession}
import org.bson.Document
import tweets.TwitterConnectionImpl

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

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

    import spark.implicits._
    val bearerToken= sys.env.getOrElse("TWITTER_BEARER","")
    println("BEARER "+bearerToken)

    val tweetDF: DataFrame = spark.readStream.
      format(providerClassName).
      option(TwitterStreamingSource.QUEUE_SIZE, 10000).

      load

    tweetDF.printSchema

    val windowedCounts = tweetDF. withColumn("timestamp",current_timestamp()).
      groupBy(window($"timestamp", "1 minutes"),
      $"party"
    ).count()

    val x= windowedCounts.writeStream
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .outputMode("update")
      .format("console")
      .start()

/*
    val x: StreamingQuery =tweetDF.writeStream.foreach(new MongoForEachWriter)
      .outputMode("append")
      .start()
*/
    while(true){}

    println("*******************************************************************************************")
    println("Spark Thread stopped")
    println("*******************************************************************************************")
    x.stop
    TwitterConnectionImpl.stop
    spark.stop

  }
}