package tweets

import org.bson.Document

import java.util

case class Domain(id: String, name: String, description: String)
case class Entity(id: String, name: String)
case class Context(domain: Domain, entity: Entity)

// TODO Entities represents a complex twitter entity annotation
case class Entities()

case class Tweet(
                  id:String,
                  text: String,
                  createdDate:String,
                  context: List[Context],
                  entities: List[Entities],
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

  }
}
