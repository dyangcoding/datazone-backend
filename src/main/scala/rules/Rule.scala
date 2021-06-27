package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/*
 Represents data required for filtered Stream Endpoint, only keyword is required, others are optional
 */
final case class Rule (
     keyword:         String,
     userId:          Option[Long] = None,
     emoji:           Option[String] = None,
     phrase:          Option[String] = None,
     hashtags:        Option[String] = None,
     fromUser:        Option[String] = None,
     toUser:          Option[String] = None,
     url:             Option[String] = None,
     retweetsOfUser:  Option[String] = None,
     context:         Option[String] = None,
     entity:          Option[String] = None,
     conversationId:  Option[Long] = None) {

  def toBasicPayload: RulePayload = {
    RulePayload(keyword)
  }

  def toPayload: RulePayload = {
    ???
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ruleFormat: RootJsonFormat[Rule] = jsonFormat12(Rule)
}