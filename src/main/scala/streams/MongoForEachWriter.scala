package streams

import scala.concurrent.ExecutionContext.Implicits.global
import db.TweetService
import org.apache.spark.sql.{ForeachWriter, _}
import org.apache.spark.internal.Logging
import tweets.Tweet

import scala.collection.mutable
import scala.util.{Failure, Success}

class MongoForEachWriter extends ForeachWriter[Row] with Logging {
  var tweetList: mutable.ArrayBuffer[Row] = _

  override def open(partitionId: Long, version: Long): Boolean = {
    tweetList = new mutable.ArrayBuffer[Row]()
    true
  }

  override def process(value: Row): Unit = {
    tweetList.append(value)
  }

  override def close(errorOrNull: Throwable): Unit = {
    if (tweetList.nonEmpty) {
      try {
        val tweets = tweetList.map(row => Tweet.createTweetFromRow(row)).toList
        tweets.foreach(tweet =>
          TweetService.InsertOne(tweet).onComplete{
            case Success(_) => log.info("Insert Tweet into DB")
            case Failure(exception) => log.error("Can not perform inserting into DB, reason: " + exception.toString)
          }
        )
      } catch {
        case exception: Exception => log.error(exception.toString)
      }
    }
  }
}
