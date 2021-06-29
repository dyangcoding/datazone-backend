package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import utils.StringUtils._

trait Rule {
  def toPayload: PayloadEntry
}

abstract class BasicRule(val keyword:        String,
                         val emoji:          Option[String]=None,
                         val mentionedUserId: Option[String]=None) extends Rule {

  override def toPayload: PayloadEntry = {
    val emojiPayload = emoji match {
      case Some(emoji: String) =>
        PayloadEntry(value = Group(keyword))
        .flatMap(payload => PayloadEntry(And(payload.value, emoji)))
      case _ => PayloadEntry(value = Group(keyword))
    }

    mentionedUserId match {
      case Some(containsUserId: String) =>
        emojiPayload
          .flatMap(payload => PayloadEntry(And(payload.value, AppendAt(containsUserId))))
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
      case Some(phrase: String) =>
        StandardRule.super.toPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Group(phrase))))
      case _ => StandardRule.super.toPayload
    }

    val hashtagPayload = hashtags match {
      case Some(hashtags: String) =>
        phrasePayload
          .flatMap(payload => PayloadEntry(And(payload.value, hashtags)))
      case _ => phrasePayload
    }

    url match {
      case Some(url: String) =>
        hashtagPayload
          .flatMap(payload => PayloadEntry(And(payload.value, url)))
      case _ => hashtagPayload
    }
  }

  def toStandardRule: PayloadEntry = toPayload
}

abstract class AdvancedRule(override val keyword:        String,
                            override val emoji:          Option[String]=None,
                            override val mentionedUserId: Option[String]=None,
                            override val phrase:         Option[String]=None,
                            override val hashtags:       Option[String]=None,
                            override val url:            Option[String]=None,
                            val fromUser:                Option[String]=None, // excluding the @ character or the user's numeric user ID
                            val toUser:                  Option[String]=None, // excluding the @ character or the user's numeric user ID
                            val retweetsOfUser:          Option[String]=None  // excluding the @ character or the user's numeric user ID
              ) extends StandardRule(keyword, emoji, mentionedUserId, phrase, hashtags, url) {

  override def toPayload: PayloadEntry = {
    val fromUserPayload = fromUser match {
      case Some(fromUser: String) =>
        AdvancedRule.super.toPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Append("from:", fromUser))))
      case _ => AdvancedRule.super.toPayload
    }

    val toUserPayload = toUser match {
      case Some(toUser: String) =>
        fromUserPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Append("to:", toUser))))
      case _ => fromUserPayload
    }

    retweetsOfUser match {
      case Some(retweetsOfUser: String) =>
        toUserPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Append("retweetOf:", retweetsOfUser))))
      case _ => toUserPayload
    }
  }

  def toAdvancedRule: PayloadEntry = toPayload
}

case class FullRule(override val keyword:        String,
                    override val emoji:          Option[String]=None,
                    override val mentionedUserId: Option[String]=None,
                    override val phrase:         Option[String]=None,
                    override val hashtags:       Option[String]=None,
                    override val url:            Option[String]=None,
                    override val fromUser:       Option[String]=None,
                    override val toUser:         Option[String]=None,
                    override val retweetsOfUser: Option[String]=None,
                    context:                     Option[String]=None,
                    entity:                      Option[String]=None,
                    conversationId:              Option[String]=None // matches Tweets that share a common conversation ID
                  ) extends AdvancedRule(keyword, emoji, mentionedUserId, phrase, hashtags, url, fromUser, toUser, retweetsOfUser) {

  override def toPayload: PayloadEntry = {
    val contextPayload = context match {
      case Some(context: String) =>
        FullRule.super.toPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Append("context:", context))))
      case _ => FullRule.super.toPayload
    }

    val entityPayload = entity match {
      case Some(entity: String) =>
        contextPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Append("entity:", entity))))
      case _ => contextPayload
    }

    conversationId match {
      case Some(conversationId: String) =>
        entityPayload
          .flatMap(payload => PayloadEntry(And(payload.value, Append("conversationId:", conversationId))))
      case _ => entityPayload
    }
  }
  }
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val fullRuleFormat: RootJsonFormat[FullRule] = jsonFormat12(FullRule)
}