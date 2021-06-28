package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import utils.StringUtils._

trait Rule {
  val isRetweet:  Option[Boolean]=Option(false)
  val isVerified: Option[Boolean]=Option(true)
  def toPayload: PayloadEntry
}

case class BasicRule(keyword: String, emoji: String, containsUserId: String) extends Rule {
  override def toPayload: PayloadEntry =
    PayloadEntry(value = Group(keyword))
      .flatMap(payload => PayloadEntry(And(emoji, payload.value)))
      .flatMap(payload => PayloadEntry(And(AppendAt(containsUserId), payload.value)))

  def toBasicRule: PayloadEntry = toPayload
}

case class StandardRule(override val keyword: String,
                   override val emoji: String,
                   override val containsUserId: String, // including the @ character
                   phrase: String,
                   hashtags: String,
                   url: String) extends BasicRule(keyword, emoji, containsUserId) {

  val isReply: Option[Boolean]=Option(false)
  val hasHashtags: Option[Boolean]=Option(true)

  override def toPayload: PayloadEntry =
    toBasicRule
      .flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
      .flatMap(payload => PayloadEntry(And(hashtags, payload.value)))
      .flatMap(payload => PayloadEntry(And(url, payload.value)))

  def toStandardRule: PayloadEntry = toPayload
}

case class AdvancedRule(override val keyword: String,
                   override val emoji: String,
                   override val containsUserId: String,
                   override val phrase: String,
                   override val hashtags: String,
                   override val url: String,
                   fromUser: String, // excluding the @ character or the user's numeric user ID
                   toUser: String,  // excluding the @ character or the user's numeric user ID
                   retweetsOfUser: String // excluding the @ character or the user's numeric user ID
              ) extends StandardRule(keyword, emoji, containsUserId, phrase, hashtags, url) {

  val hasLinks: Option[Boolean]=Option(true)
  val hasMedia: Option[Boolean]=Option(true)
  val hasImages: Option[Boolean]=Option(true)

  override def toPayload: PayloadEntry =
    toStandardRule
      .flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
      .flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
      .flatMap(payload => PayloadEntry(And(Append("retweetOf:", retweetsOfUser), payload.value)))

  def toAdvancedRule: PayloadEntry = toPayload
}

case class FullRule(override val keyword: String,
               override val emoji: String,
               override val containsUserId: String,
               override val phrase: String,
               override val hashtags: String,
               override val url: String,
               override val fromUser: String,
               override val toUser: String,
               override val retweetsOfUser: String,
               context: String,
               entity: String,
               conversationId: String // matches Tweets that share a common conversation ID
            ) extends AdvancedRule(keyword, emoji, containsUserId, phrase, hashtags, url, fromUser, toUser, retweetsOfUser) {
  val hasVideos: Option[Boolean]=Option(false)

  override def toPayload: PayloadEntry =
    toAdvancedRule
      .flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
      .flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
      .flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val basicRuleFormat: RootJsonFormat[BasicRule] = jsonFormat3(BasicRule)
  implicit val standardRuleFormat: RootJsonFormat[StandardRule] = jsonFormat6(StandardRule)
  implicit val advancedRuleFormat: RootJsonFormat[AdvancedRule] = jsonFormat9(AdvancedRule)
  implicit val fullRuleFormat: RootJsonFormat[FullRule] = jsonFormat12(FullRule)
}