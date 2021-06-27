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

  def appendEmoji(emoji: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(emoji, payload.value)))
  }

  def appendUrl(url: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(url, payload.value)))
  }

  def appendPhrase(phrase: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(phrase, payload.value)))
  }

  def appendContext(context: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(Append("context:", context), payload.value)))
  }

  def appendEntity(entity: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(Append("entity:", entity), payload.value)))
  }

  def appendConversationId(conversationId: String): RulePayload = {
    this.flatMap(payload => RulePayload(And(Append("conversationId:", conversationId), payload.value)))
  }

  def group(): RulePayload = {
    this.flatMap(payload => RulePayload(Group(payload.value)))
  }
}

/*
 Represents optional options for building rules for filtered Stream Endpoint
 */
case class RuleOptions(
     isRetweet:   Option[Boolean] = Option(false),
     isReply:     Option[Boolean] = Option(false),
     isVerified:  Option[Boolean] = Option(true),
     hasHashTags: Option[Boolean] = Option(true),
     hasLinks:    Option[Boolean] = Option(true),
     hasMedia:    Option[Boolean] = Option(true),
     hasImages:   Option[Boolean] = Option(true),
     hasVideos:   Option[Boolean] = Option(false))

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
     conversationId:  Option[Long] = None,
     lang:            Option[String] = Option("de"),
     ruleOptions:     RuleOptions = RuleOptions()) {

  def toBasicPayload: RulePayload = {
    RulePayload(keyword)
  }

  def toPayload: RulePayload = {
    ???
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ruleOptionsFormat: RootJsonFormat[RuleOptions] = jsonFormat8(RuleOptions)
  implicit val ruleFormat: RootJsonFormat[Rule] = jsonFormat14(Rule)
}