package db

import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.bson.collection.BSONCollection
import tweets.Tweet

object TweetService {
  val collection: Future[BSONCollection] = DBUtil.GetCollection("tweets")

  def InsertOne(tweet: Tweet): Future[WriteResult] = {
    collection.flatMap(_.insert.one(tweet))
  }

  def FetchAll(): Future[Seq[Tweet]] = {
    collection.flatMap(_.find(BSONDocument())
      .cursor[Tweet](ReadPreference.primary)
      .collect[Seq](Int.MaxValue, Cursor.FailOnError[Seq[Tweet]]())
    )
  }
}
