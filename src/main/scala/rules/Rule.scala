package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class Rule(
     keyword: String, userID: Long, emoji: String,
     phrase: String, hashtags: String, fromUser: String, toUser: String,
     url: String, retweetsOfUser: String, context: String, entity: String,
     conversationId: Long) {
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ruleFormat: RootJsonFormat[Rule] = jsonFormat12(Rule)
}
