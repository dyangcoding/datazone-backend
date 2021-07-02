package db

import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}

trait DBUtil {
  def GetCollection(collection: String): Future[BSONCollection]
}

object DBUtil extends DBUtil {
  // My settings (see available connection options)
  val mongoUri = "mongodb://localhost:27017/dataZone"

  // TODO: maybe application context
  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver: AsyncDriver = AsyncDriver()

  // Database and collections: Get references
  val futureConnection: Future[MongoConnection] = driver.connect(mongoUri)
  def dataZone: Future[DB] = futureConnection.flatMap(_.database("dataZone"))

  override def GetCollection(collection: String): Future[BSONCollection] =
    dataZone.map(_.collection(collection))
}
