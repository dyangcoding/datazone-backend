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

case class BasicRule(keyword: String, emoji: Option[String], containsUserId: Option[String]) extends Rule {
  override def toPayload: PayloadEntry = (emoji, containsUserId) match {
    case (Some(emoji: String), Option.empty) =>
      PayloadEntry(value = Group(keyword))
        .flatMap(payload => PayloadEntry(And(emoji, payload.value)))
    case (Option.empty, Some(containsUserId: String)) =>
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

case class StandardRule(override val keyword: String,
                   override val emoji: Option[String],
                   override val containsUserId: Option[String], // including the @ character
                   phrase: Option[String],
                   hashtags: Option[String],
                   url: Option[String]) extends BasicRule(keyword, emoji, containsUserId) {

  val isReply: Option[Boolean]=Option(false)
  val hasHashtags: Option[Boolean]=Option(true)

  override def toPayload: PayloadEntry = (phrase, hashtags, url) match {
    case (Some(phrase: String), Option.empty, Option.empty) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
    case (Option.empty, Some(hashtags: String), Option.empty) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
    case (Option.empty, Option.empty, Some(url: String)) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case (Some(phrase: String), Some(hashtags: String), Option.empty) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
    case (Some(phrase: String), Option.empty, Some(url: String)) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case (Option.empty, Some(hashtags: String), Some(url: String)) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case (Some(phrase: String), Some(hashtags: String), Some(url: String)) =>
      toBasicRule
        .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
        .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
        .flatMap(payload => PayloadEntry(And(url, payload.value)))
    case _ =>
      toBasicRule
  }

  def toStandardRule: PayloadEntry = toPayload
}

case class AdvancedRule(override val keyword: String,
                   override val emoji: Option[String],
                   override val containsUserId: Option[String],
                   override val phrase: Option[String],
                   override val hashtags: Option[String],
                   override val url: Option[String],
                   fromUser: Option[String], // excluding the @ character or the user's numeric user ID
                   toUser: Option[String],  // excluding the @ character or the user's numeric user ID
                   retweetsOfUser: Option[String] // excluding the @ character or the user's numeric user ID
              ) extends StandardRule(keyword, emoji, containsUserId, phrase, hashtags, url) {

  val hasLinks: Option[Boolean]=Option(true)
  val hasMedia: Option[Boolean]=Option(true)
  val hasImages: Option[Boolean]=Option(true)

  override def toPayload: PayloadEntry = (fromUser, toUser, retweetsOfUser) match {
    case (Some(fromUser: String), Option.empty, Option.empty) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
    case (Option.empty, Some(toUser: String), Option.empty) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
    case (Option.empty, Option.empty, Some(retweetsOfUser: String)) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case (Some(fromUser: String), Some(toUser: String), Option.empty) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
    case (Some(fromUser: String), Option.empty, Some(retweetsOfUser: String)) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case (Option.empty, Some(toUser: String), Some(retweetsOfUser: String)) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case (Some(fromUser: String), Some(toUser: String), Some(retweetsOfUser: String)) =>
      toStandardRule
        .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))
    case _ =>
      toStandardRule
  }

  def toAdvancedRule: PayloadEntry = toPayload
}

case class FullRule(override val keyword: String,
               override val emoji: Option[String],
               override val containsUserId: Option[String],
               override val phrase: Option[String],
               override val hashtags: Option[String],
               override val url: Option[String],
               override val fromUser: Option[String],
               override val toUser: Option[String],
               override val retweetsOfUser: Option[String],
               context: Option[String],
               entity: Option[String],
               conversationId: Option[String] // matches Tweets that share a common conversation ID
            ) extends AdvancedRule(keyword, emoji, containsUserId, phrase, hashtags, url, fromUser, toUser, retweetsOfUser) {
  val hasVideos: Option[Boolean]=Option(false)

  override def toPayload: PayloadEntry = (context, entity, conversationId) match {
    case (Some(context: String), Option.empty, Option.empty) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
    case (Option.empty, Some(entity: String), Option.empty) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
    case (Option.empty, Option.empty, Some(conversationId: String)) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case (Some(context: String), Some(entity: String), Option.empty) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
    case (Some(context: String), Option.empty, Some(conversationId: String)) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case (Option.empty, Some(entity: String), Some(conversationId: String)) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case (Some(context: String), Some(entity: String), Some(conversationId: String)) =>
      toAdvancedRule
        .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
        .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
    case _ =>
      toAdvancedRule
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val basicRuleFormat: RootJsonFormat[BasicRule] = jsonFormat3(BasicRule)
  implicit val standardRuleFormat: RootJsonFormat[StandardRule] = jsonFormat6(StandardRule)
  implicit val advancedRuleFormat: RootJsonFormat[AdvancedRule] = jsonFormat9(AdvancedRule)
  implicit val fullRuleFormat: RootJsonFormat[FullRule] = jsonFormat12(FullRule)
}