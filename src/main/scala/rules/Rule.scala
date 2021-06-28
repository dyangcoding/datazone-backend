package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import utils.StringUtils._

trait Rule {
  val isRetweet:  Option[Boolean]=Option(false)
  val isVerified: Option[Boolean]=Option(true)
  val simple: Int=30
  def toPayload: PayloadEntry
}

abstract class BasicRule(val keyword:        String,
                         val emoji:          Option[String]=None,
                         val containsUserId: Option[String]=None) extends Rule {
  override def toPayload: PayloadEntry = (emoji, containsUserId) match {
    case (Some(emoji: String), None) =>
      PayloadEntry(value = Group(keyword))
        .flatMap(payload => PayloadEntry(And(emoji, payload.value)))
    case (None, Some(containsUserId: String)) =>
      PayloadEntry(value = Group(keyword))
        .flatMap(payload => PayloadEntry(And(AppendAt(containsUserId), payload.value)))
    case (Some(emoji: String), Some(containsUserId: String)) =>
      PayloadEntry(value = Group(keyword))
        .flatMap(payload => PayloadEntry(And(emoji, payload.value)))
        .flatMap(payload => PayloadEntry(And(AppendAt(containsUserId), payload.value)))
    case _ =>
      PayloadEntry(value = Group(keyword))
  }

  def toBasicRule: PayloadEntry = toPayload
}

abstract class StandardRule(override val keyword: String,
                            override val emoji: Option[String]=None,
                            override val containsUserId: Option[String]=None, // including the @ character
                            val phrase: Option[String]=None,
                            val hashtags: Option[String]=None,
                            val url: Option[String]=None) extends BasicRule(keyword, emoji, containsUserId) {

  val isReply: Option[Boolean]=Option(false)
  val hasHashtags: Option[Boolean]=Option(true)

  override def toPayload: PayloadEntry = (phrase, hashtags, url) match {
    case (Some(phrase: String), None, None) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
    case (None, Some(hashtags: String), None) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
    case (None, None, Some(url: String)) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case (Some(phrase: String), Some(hashtags: String), None) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
    case (Some(phrase: String), None, Some(url: String)) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case (None, Some(hashtags: String), Some(url: String)) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case (Some(phrase: String), Some(hashtags: String), Some(url: String)) =>
      StandardRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case _ =>
      StandardRule.super.toPayload
  }

  def toStandardRule: PayloadEntry = toPayload
}

abstract class AdvancedRule(override val keyword:        String,
                            override val emoji:          Option[String]=None,
                            override val containsUserId: Option[String]=None,
                            override val phrase:         Option[String]=None,
                            override val hashtags:       Option[String]=None,
                            override val url:            Option[String]=None,
                            val fromUser:                Option[String]=None, // excluding the @ character or the user's numeric user ID
                            val toUser:                  Option[String]=None, // excluding the @ character or the user's numeric user ID
                            val retweetsOfUser:          Option[String]=None // excluding the @ character or the user's numeric user ID
              ) extends StandardRule(keyword, emoji, containsUserId, phrase, hashtags, url) {

  val hasLinks: Option[Boolean]=Option(true)
  val hasMedia: Option[Boolean]=Option(true)
  val hasImages: Option[Boolean]=Option(true)

  override def toPayload: PayloadEntry = (fromUser, toUser, retweetsOfUser) match {
    case (Some(fromUser: String), None, None) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
    case (None, Some(toUser: String), None) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
    case (None, None, Some(retweetsOfUser: String)) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case (Some(fromUser: String), Some(toUser: String), None) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
    case (Some(fromUser: String), None, Some(retweetsOfUser: String)) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case (None, Some(toUser: String), Some(retweetsOfUser: String)) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case (Some(fromUser: String), Some(toUser: String), Some(retweetsOfUser: String)) =>
      AdvancedRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case _ =>
      AdvancedRule.super.toPayload
  }

  def toAdvancedRule: PayloadEntry = toPayload
}

case class FullRule(override val keyword:        String,
                    override val emoji:          Option[String]=None,
                    override val containsUserId: Option[String]=None,
                    override val phrase:         Option[String]=None,
                    override val hashtags:       Option[String]=None,
                    override val url:            Option[String]=None,
                    override val fromUser:       Option[String]=None,
                    override val toUser:         Option[String]=None,
                    override val retweetsOfUser: Option[String]=None,
                    context:                     Option[String]=None,
                    entity:                      Option[String]=None,
                    conversationId:              Option[String]=None // matches Tweets that share a common conversation ID
                  ) extends AdvancedRule(keyword, emoji, containsUserId, phrase, hashtags, url, fromUser, toUser, retweetsOfUser) {
  val hasVideos: Option[Boolean]=Option(false)

  override def toPayload: PayloadEntry = (context, entity, conversationId) match {
    case (Some(context: String), None, None) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
    case (None, Some(entity: String), None) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
    case (None, None, Some(conversationId: String)) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case (Some(context: String), Some(entity: String), None) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
    case (Some(context: String), None, Some(conversationId: String)) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case (None, Some(entity: String), Some(conversationId: String)) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case (Some(context: String), Some(entity: String), Some(conversationId: String)) =>
      FullRule.super.toPayload
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case _ =>
      FullRule.super.toPayload
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val fullRuleFormat: RootJsonFormat[FullRule] = jsonFormat12(FullRule)
}