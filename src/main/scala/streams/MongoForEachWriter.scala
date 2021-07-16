package streams

import db.TweetService
import org.apache.spark.sql.{ForeachWriter, _}
import tweets.Tweet

class MongoForEachWriter extends ForeachWriter[Row] {
  override def open(partitionId: Long, version: Long): Boolean = {
    true
  }

  override def process(value: Row): Unit = {
    val tweet: Tweet = Tweet.createTweetFromRow(value)
    TweetService.InsertOne(tweet)
  }

  override def close(errorOrNull: Throwable): Unit = {
    // do nothing
  }
}
