package db

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}
import tweets.Tweet

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object TweetService {
  val collection: Future[BSONCollection] = DBUtil.GetCollection("tweets")

  def InsertOne(tweet: Tweet): Future[WriteResult] = {
    collection.flatMap(_.insert.one(tweet))
  }

  def InsertMany(tweets: Seq[Tweet]): Unit = {
    collection.flatMap(_.insert(ordered = false).many(tweets)).onComplete{
      case Success(value) => println("")
      case Failure(exception) => println("")
    }
  }

  def FetchAll(): Future[Seq[Tweet]] = {
    collection.flatMap(_.find(BSONDocument())
      .cursor[Tweet](ReadPreference.primary)
      .collect[Seq](Int.MaxValue, Cursor.FailOnError[Seq[Tweet]]())
    )
  }
}
