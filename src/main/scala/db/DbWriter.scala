package db

import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{AsyncDriver, Collection, Cursor, DB, MongoConnection}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, Macros, document}
import tweets.Tweet

object DbWriter {
  // My settings (see available connection options)
  val mongoUri = "mongodb://localhost:27017/dataZone?authMode=scram-sha1"

  // TODO: maybe application context
  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver: AsyncDriver = AsyncDriver()
  val parsedUri: Future[ParsedURI] = MongoConnection.fromString(mongoUri)

  // Database and collections: Get references
  val futureConnection: Future[MongoConnection] = parsedUri.flatMap(driver.connect)
  def dataZone: Future[DB] = futureConnection.flatMap(_.database("dataZone"))

  def tweetsCollection: Future[BSONCollection] = dataZone.map(_.collection("tweets"))

  // Write Documents: insert
  implicit def twitterWriter: BSONDocumentWriter[Tweet] = Macros.writer[Tweet]
  // or provide a custom one

  // use personWriter
  def createTweet(tweet: Tweet): Future[Unit] =
    tweetsCollection.flatMap(_.insert.one(tweet).map(_ => {}))

  implicit def tweetsReader: BSONDocumentReader[Tweet] = Macros.reader[Tweet]
  // or provide a custom one
}