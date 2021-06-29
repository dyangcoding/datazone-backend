package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Rule(keyword:         String,
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
                conversationId:  Option[String]=None // matches Tweets that share a common conversation ID
){

  // starting point to build a PayloadEntry
  def toBasicPayload: PayloadEntry = PayloadEntry(value = keyword).group()

  def applyEmoji(payload: PayloadEntry): PayloadEntry = {
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

  // TODO: apply Rule Options to Payload
  def applyOptions(entry: PayloadEntry): PayloadEntry = {
    ???
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val fullRuleFormat: RootJsonFormat[Rule] = jsonFormat12(Rule)
}