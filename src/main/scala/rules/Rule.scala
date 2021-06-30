package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class RuleOptions(isRetweet:    Option[Boolean]=None, // match Tweets that are truly retweets
                       isVerified:   Option[Boolean]=None, // deliver only Tweets whose authors are verified by Twitter
                       isReply:      Option[Boolean]=None, // deliver only explicit replies that match a rule
                       hasHashtags:  Option[Boolean]=None, // match Tweets that contain at least one hashtag
                       hasLinks:     Option[Boolean]=None, // match Tweets which contain links and media in the Tweet body
                       hasMedia:     Option[Boolean]=None, // match Tweets that contain a media object, such as a photo, GIF, or video, as determined by Twitter
                       hasImages:    Option[Boolean]=None, // match Tweets that contain a recognized URL to an image
                       hasVideos:    Option[Boolean]=None, // match Tweets that contain native Twitter videos, uploaded directly to Twitter
                       sample:       Option[Int]    =None  // a random percent sample of Tweets that match a rule rather than the entire set of Tweets
                      ) {

  private def applyIsRetweet(payload: PayloadEntry): PayloadEntry = {
    isRetweet match {
      case Some(isRetweet: Boolean) => payload.applyIsRetweet(isRetweet)
      case _ => payload
    }
  }

  private def applyIsVerified(payload: PayloadEntry): PayloadEntry = {
    isVerified match {
      case Some(isVerified: Boolean) => payload.applyIsVerified(isVerified)
      case _ => payload
    }
  }

  private def applyIsReply(payload: PayloadEntry): PayloadEntry = {
    isReply match {
      case Some(isReply: Boolean) => payload.applyIsReply(isReply)
      case _ => payload
    }
  }

  private def applyHasHashtags(payload: PayloadEntry): PayloadEntry = {
    hasHashtags match {
      case Some(hasHashtags: Boolean) => payload.applyHasHashtags(hasHashtags)
      case _ => payload
    }
  }

  private def applyHasLinks(payload: PayloadEntry): PayloadEntry = {
    hasLinks match {
      case Some(hasLinks: Boolean) => payload.applyHasLinks(hasLinks)
      case _ => payload
    }
  }

  private def applyHasMedia(payload: PayloadEntry): PayloadEntry = {
    hasMedia match {
      case Some(hasHasMedia: Boolean) => payload.applyHasMedia(hasHasMedia)
      case _ => payload
    }
  }

  private def applyHasImages(payload: PayloadEntry): PayloadEntry = {
    hasImages match {
      case Some(hasImage: Boolean) => payload.applyHasImages(hasImage)
      case _ => payload
    }
  }

  private def applyHasVideos(payload: PayloadEntry): PayloadEntry = {
    hasVideos match {
      case Some(hasVideos: Boolean) => payload.applyHasVideos(hasVideos)
      case _ => payload
    }
  }

  private def applySample(payload: PayloadEntry): PayloadEntry = {
    sample match {
      case Some(simple: Int) => payload.applySample(simple)
      case _ => payload.applySample(30)
    }
  }

  def applyOptions(payload: PayloadEntry): PayloadEntry = {
    payload
      .flatMap(payload => applyIsRetweet(payload))
      .flatMap(payload => applyIsVerified(payload))
      .flatMap(payload => applyIsReply(payload))
      .flatMap(payload => applyHasHashtags(payload))
      .flatMap(payload => applyHasLinks(payload))
      .flatMap(payload => applyHasMedia(payload))
      .flatMap(payload => applyHasImages(payload))
      .flatMap(payload => applyHasVideos(payload))
      .flatMap(payload => applySample(payload))
  }
}

case class Rule(keyword:         Option[String]=None,
                emoji:           Option[String]=None, // matches a keyword within the body of a Tweet
                mentionedUserId: Option[String]=None, // including the @ character
                phrase:          Option[String]=None, // matches the exact phrase within the body of a Tweet
                hashtags:        Option[String]=None, // matches any Tweet containing a recognized hashtag
                url:             Option[String]=None, // performs a tokenized match on any validly-formatted URL of a Tweet
                fromUser:        Option[String]=None, // excluding the @ character or the user's numeric user ID
                toUser:          Option[String]=None, // excluding the @ character or the user's numeric user ID
                retweetsOfUser:  Option[String]=None, // excluding the @ character or the user's numeric user ID
                context:         Option[String]=None, // matches Tweets with a specific domain id and/or domain id
                entity:          Option[String]=None, // matches Tweets with a specific entity string value
){

  // starting point to build a PayloadEntry
  def toBasicPayload: PayloadEntry = PayloadEntry(value = "")

  private def applyKeyword(payload: PayloadEntry): PayloadEntry = {
    keyword match {
      case Some(keyword: String) =>
        payload.applyKeyword(keyword)
      case _ => payload
    }
  }

  private def applyEmoji(payload: PayloadEntry): PayloadEntry = {
    emoji match {
      case Some(emoji: String) =>
        payload.applyEmoji(emoji)
      case _ => payload
    }
  }

  def applyMentionedUserId(payload: PayloadEntry): PayloadEntry = {
    mentionedUserId match {
      case Some(mentionedUserId: String) => payload.applyUserId(mentionedUserId)
      case _ => payload
    }
  }

  def applyPhrase(payload: PayloadEntry): PayloadEntry = {
    phrase match {
      case Some(phrase: String) => payload.applyPhrase(phrase)
      case _ => payload
    }
  }

  def applyHashTags(payload: PayloadEntry): PayloadEntry = {
    hashtags match {
      case Some(hashtags: String) => payload.applyHashtag(hashtags)
      case _ => payload
    }
  }

  def applyUrl(payload: PayloadEntry): PayloadEntry = {
    url match {
      case Some(url: String) => payload.applyUrl(url)
      case _ => payload
    }
  }

  def applyFromUser(payload: PayloadEntry): PayloadEntry = {
    fromUser match {
      case Some(fromUser: String) => payload.applyFromUser(fromUser)
      case _ => payload
    }
  }

  def applyToUser(payload: PayloadEntry): PayloadEntry = {
    toUser match {
      case Some(toUser: String) => payload.applyToUser(toUser)
      case _ => payload
    }
  }

  def applyRetweetsOfUser(payload: PayloadEntry): PayloadEntry = {
    retweetsOfUser match {
      case Some(retweetsOfUser: String) => payload.applyRetweetsOfUser(retweetsOfUser)
      case _ => payload
    }
  }

  def applyContext(payload: PayloadEntry): PayloadEntry = {
    context match {
      case Some(context: String) => payload.applyContext(context)
      case _ => payload
    }
  }

  def applyEntity(payload: PayloadEntry): PayloadEntry = {
    entity match {
      case Some(entity: String) => payload.applyEntity(entity)
      case _ => payload
    }
  }

  def applyConversationId(payload: PayloadEntry): PayloadEntry = {
    conversationId match {
      case Some(conversationId: String) => payload.applyConversationId(conversationId)
      case _ => payload
    }
  }

  def toPayload: PayloadEntry = {
    toBasicPayload
      .flatMap(payload => applyEmoji(payload))
      .flatMap(payload => applyMentionedUserId(payload))
      .flatMap(payload => applyPhrase(payload))
      .flatMap(payload => applyHashTags(payload))
      .flatMap(payload => applyUrl(payload))
      .flatMap(payload => applyFromUser(payload))
      .flatMap(payload => applyToUser(payload))
      .flatMap(payload => applyRetweetsOfUser(payload))
      .flatMap(payload => applyContext(payload))
      .flatMap(payload => applyEntity(payload))
      .flatMap(payload => applyConversationId(payload))
  }

  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ruleOptionsFormat: RootJsonFormat[RuleOptions] = jsonFormat9(RuleOptions)
  implicit val fullRuleFormat: RootJsonFormat[Rule] = jsonFormat13(Rule)
}