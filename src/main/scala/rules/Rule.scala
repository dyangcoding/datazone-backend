package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import utils.StringUtils._

case class Payload(operation: String, entries: Seq[RulePayload])

case class RulePayload (value: String, tag: Option[String] = None) {
  def flatMap(transformer: rules.RulePayload => rules.RulePayload): rules.RulePayload = {
    transformer(this)
  }

  def appendHashtag(hashtag: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(hashtag, payload.value)))
  }

  def appendUserId(userId: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(AppendAt(userId), payload.value)))
  }

  def appendFromUser(fromUser: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(Append("from:", fromUser), payload.value)))
  }

  def appendToUser(toUser: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(Append("to:", toUser), payload.value)))
  }

  def appendTag(tagValue: String): RulePayload = {
    this.flatMap(payload => RulePayload(payload.value, tag=Option(tagValue)))
  }
}

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