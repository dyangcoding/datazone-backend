package db

import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{AsyncDriver, MongoConnection}
import utils.FileIO

trait DBUtil {
  def GetCollection(collection: String): Future[BSONCollection]
}

object DBUtil extends DBUtil {
  val mongoUri: String = FileIO.getMongoUri

  import ExecutionContext.Implicits.global

  // Connect to the database: Must be done only once per application
  val driver: AsyncDriver = AsyncDriver()

  override def GetCollection(collectionName: String): Future[BSONCollection] = {
    (for {
      uri   <- MongoConnection.fromString(mongoUri)
      conn  <- driver.connect(uri)
      dn    <- Future(uri.db.get)
      db    <- conn.database(dn)
    } yield db.collection(collectionName)).recover {
      case exception: Exception => throw new Exception(exception.toString)
    }
  }
}
