package tweets

import reactivemongo.api.bson._
import reactivemongo.api.bson.{BSONReader, BSONWriter, Macros}
import org.bson.Document
import reactivemongo.api.bson.Macros.Annotations.Reader
import tweets.Context.contextSeqReader
import tweets.Entities.entitiesSeqReader

import java.util
import scala.language.postfixOps

case class Domain(id: String, name: String, description: String)
case object Domain { val domain: BSONDocumentHandler[Domain] = Macros.handler[Domain] }

case class Entity(id: String, name: String)
case object Entity { val entity: BSONDocumentHandler[Entity] = Macros.handler[Entity] }

case class Context(domain: Domain, entity: Entity)
case object Context {
  implicit val domain: BSONDocumentHandler[Domain] = Macros.handler[Domain]
  implicit val entity: BSONDocumentHandler[Entity] = Macros.handler[Entity]

  val contextHandler: BSONHandler[Context] = Macros.handler[Context]

  val contextSeqReader: BSONReader[Seq[Context]] = BSONReader.iterable[Context, Seq](contextHandler readTry)
  val contextSeqWriter: BSONWriter[Seq[Context]] = BSONWriter.sequence[Context](contextHandler writeTry)
}

// TODO Entities represents a complex twitter entity annotation
case class Entities()

case object Entities {
  val entitiesHandler: BSONHandler[Entities] = Macros.handler[Entities]
  val entitiesSeqReader: BSONReader[Seq[Entities]] = BSONReader.iterable[Entities, Seq](entitiesHandler readTry)
  val entitiesSeqWriter: BSONWriter[Seq[Entities]] = BSONWriter.sequence[Entities](entitiesHandler writeTry)
}

case class Tweet(
                  id:String,
                  text: String,
                  createdDate:String,
                  @Reader(contextSeqReader) context: Seq[Context],
                  @Reader(entitiesSeqReader) entities: Seq[Entities],
                  conversationId: String,
                  lang: String) {
  def toDocument: Document = {
    val m:java.util.Map[String, Object]= new java.util.HashMap()
    m.put("id",id)
    m.put("text",text)
    m.put("createdDate",createdDate)
    val contextList = new util.ArrayList[java.util.Map[String,Object]]
    context.foreach(c => {
      val q:java.util.Map[String, Object]= new java.util.HashMap()
      q.put("domainId", c.domain.id)
      q.put("domainName", c.domain.name)
      q.put("domainDesc", c.domain.description)
      q.put("entityId", c.entity.id)
      q.put("entityName", c.entity.name)
      contextList.add(q)
    })
    // TODO add entities later on
    m.put("conversationId", conversationId)
    m.put("lang", lang)
    new Document(m)
  }
}

case object Tweet {
  def createTweet(json: String): Option[Tweet] = {
    ???
  }

  implicit val domain: BSONDocumentHandler[Domain] = Macros.handler[Domain]
  implicit val entity: BSONDocumentHandler[Entity] = Macros.handler[Entity]
  implicit val context: BSONDocumentHandler[Context] = Macros.handler[Context]
  implicit val entities: BSONDocumentHandler[Entities] = Macros.handler[Entities]
  implicit val TweetHandler: BSONDocumentHandler[Tweet] = Macros.handler[Tweet]
}
