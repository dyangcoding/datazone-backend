package tweets

import org.bson.Document

case class Tweet(id:String, text: String) {
  def toDocument: Document={
    val m:java.util.Map[String,Object]= new java.util.HashMap()
    m.put("id",id)
    m.put("text",text)
    new Document(m)
  }
}
