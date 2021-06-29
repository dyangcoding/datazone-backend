package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import utils.StringUtils._

trait Rule {
  // match Tweets that are truly retweets
  val isRetweet:    Option[Boolean]=Option(false)
  // deliver only Tweets whose authors are verified by Twitter
  val isVerified:   Option[Boolean]=Option(true)
  // deliver only explicit replies that match a rule
  val isReply:      Option[Boolean]=Option(false)
  // match Tweets that contain at least one hashtag
  val hasHashtags:  Option[Boolean]=Option(true)
  // match Tweets which contain links and media in the Tweet body
  val hasLinks:     Option[Boolean]=Option(true)
  // match Tweets that contain a media object, such as a photo, GIF, or video, as determined by Twitter
  val hasMedia:     Option[Boolean]=Option(true)
  // match Tweets that contain a recognized URL to an image.
  val hasImages:    Option[Boolean]=Option(true)
  // match Tweets that contain native Twitter videos, uploaded directly to Twitter
  val hasVideos:    Option[Boolean]=Option(false)
  // a random percent sample of Tweets that match a rule rather than the entire set of Tweets
  val simple: Int = 30
  // convert Rule Model to PayloadEntry for filtered Stream API
  def toPayload: PayloadEntry
}

abstract class BasicRule(val keyword:         String,
                         val emoji:           Option[String]=None,
                         val mentionedUserId: Option[String]=None) extends Rule {

  override def toPayload: PayloadEntry = {
    val payload = PayloadEntry(value = keyword).group()
    val emojiPayload = emoji match {
      case Some(emoji: String) =>
        payload.applyEmoji(emoji)
      case _ => payload
    }

    mentionedUserId match {
      case Some(mentionedUserId: String) => emojiPayload.applyUserId(mentionedUserId)
      case _ => emojiPayload
    }
  }

  def toBasicRule: PayloadEntry = toPayload
}

abstract class StandardRule(override val keyword:         String,
                            override val emoji:           Option[String]=None,
                            override val mentionedUserId: Option[String]=None, // including the @ character
                            val phrase:                   Option[String]=None,
                            val hashtags:                 Option[String]=None,
                            val url:                      Option[String]=None) extends BasicRule(keyword, emoji, mentionedUserId) {

  override def toPayload: PayloadEntry = {
    val phrasePayload = phrase match {
      case Some(phrase: String) => StandardRule.super.toPayload.applyPhrase(phrase)
      case _ => StandardRule.super.toPayload
    }

    val hashtagPayload = hashtags match {
      case Some(hashtags: String) => phrasePayload.applyHashtag(hashtags)
      case _ => phrasePayload
    }

    url match {
      case Some(url: String) => hashtagPayload.applyUrl(url)
      case _ => hashtagPayload
    }
  }

  def toStandardRule: PayloadEntry = toPayload
}

abstract class AdvancedRule(override val keyword:         String,
                            override val emoji:           Option[String]=None,
                            override val mentionedUserId: Option[String]=None,
                            override val phrase:          Option[String]=None,
                            override val hashtags:        Option[String]=None,
                            override val url:             Option[String]=None,
                            val fromUser:                 Option[String]=None, // excluding the @ character or the user's numeric user ID
                            val toUser:                   Option[String]=None, // excluding the @ character or the user's numeric user ID
                            val retweetsOfUser:           Option[String]=None  // excluding the @ character or the user's numeric user ID
              ) extends StandardRule(keyword, emoji, mentionedUserId, phrase, hashtags, url) {

  override def toPayload: PayloadEntry = {
    val fromUserPayload = fromUser match {
      case Some(fromUser: String) => AdvancedRule.super.toPayload.applyFromUser(fromUser)
      case _ => AdvancedRule.super.toPayload
    }

    val toUserPayload = toUser match {
      case Some(toUser: String) => fromUserPayload.applyToUser(toUser)
      case _ => fromUserPayload
    }

    retweetsOfUser match {
      case Some(retweetsOfUser: String) => toUserPayload.applyRetweetsOfUser(retweetsOfUser)
      case _ => toUserPayload
    }
  }

  def toAdvancedRule: PayloadEntry = toPayload
}

case class FullRule(override val keyword:         String,
                    override val emoji:           Option[String]=None,
                    override val mentionedUserId: Option[String]=None,
                    override val phrase:          Option[String]=None,
                    override val hashtags:        Option[String]=None,
                    override val url:             Option[String]=None,
                    override val fromUser:        Option[String]=None,
                    override val toUser:          Option[String]=None,
                    override val retweetsOfUser:  Option[String]=None,
                    context:                      Option[String]=None,
                    entity:                       Option[String]=None,
                    conversationId:               Option[String]=None // matches Tweets that share a common conversation ID
                  ) extends AdvancedRule(keyword, emoji, mentionedUserId, phrase, hashtags, url, fromUser, toUser, retweetsOfUser) {

  override def toPayload: PayloadEntry = {
    val contextPayload = context match {
      case Some(context: String) => FullRule.super.toPayload.applyContext(context)
      case _ => FullRule.super.toPayload
    }

    val entityPayload = entity match {
      case Some(entity: String) => contextPayload.applyEntity(entity)
      case _ => contextPayload
    }

    conversationId match {
      case Some(conversationId: String) => entityPayload.applyConversationId(conversationId)
      case _ => entityPayload
    }
  }

  // TODO: apply Rule Options to Payload
  def applyOptions(entry: PayloadEntry): PayloadEntry = {
    ???
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val fullRuleFormat: RootJsonFormat[FullRule] = jsonFormat12(FullRule)
}