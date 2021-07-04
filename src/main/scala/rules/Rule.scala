package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import reactivemongo.api.bson.{BSONDocumentHandler, BSONObjectID, Macros}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat, deserializationError}
import utils.JSONParser

import scala.util.{Failure, Success}

case class Rule(_id:             Option[BSONObjectID]=None,      // require internal for mongo
                keyword:         Option[String]      =None, // matches a keyword within the body of a Tweet
                emoji:           Option[String]      =None,
                mentionedUserId: Option[String]      =None, // including the @ character
                phrase:          Option[String]      =None, // matches the exact phrase within the body of a Tweet
                hashtags:        Option[String]      =None, // matches any Tweet containing a recognized hashtag
                url:             Option[String]      =None, // performs a tokenized match on any validly-formatted URL of a Tweet
                fromUser:        Option[String]      =None, // excluding the @ character or the user's numeric user ID
                toUser:          Option[String]      =None, // excluding the @ character or the user's numeric user ID
                retweetsOfUser:  Option[String]      =None, // excluding the @ character or the user's numeric user ID
                context:         Option[String]      =None, // matches Tweets with a specific domain id and/or domain id
                entity:          Option[String]      =None, // matches Tweets with a specific entity string value
                conversationId:  Option[String]      =None, // matches Tweets that share a common conversation ID
                options:         Option[RuleOptions] =None
){
  // prohibit client sending empty Rule Data, also Rule Options can not be utilised alone for building rules
  require(atLeastOne(), "Provide at least One possible Rule Operator to effectively match any Tweets.")

  // Rule data validation
  require(keyword.isEmpty || (keyword.forall(_.nonEmpty) && keyword.forall(text => text.length <= 256)),
        "Keyword must not be empty and not longer than 256 characters.")

  require(mentionedUserId.isEmpty || (mentionedUserId.forall(_.nonEmpty) && mentionedUserId.exists(text => text.startsWith("@"))),
        "MentionedUserId must not be empty and start with '@'.")

  require(hashtags.isEmpty || (hashtags.forall(_.nonEmpty) && hashtags.exists(text => text.startsWith("#"))),
        "Hashtags must not be empty and start with '#'.")

  require(url.isEmpty || (url.forall(_.nonEmpty) && url.exists(text => text.startsWith("https://") || text.startsWith("http://"))),
        "Url must not be empty and start with 'https://' or 'http://'.")

  require(fromUser.isEmpty || (fromUser.forall(_.nonEmpty) && fromUser.exists(text => !text.startsWith("@"))),
        "FromUser must not be empty and not start with '@'.")

  require(toUser.isEmpty || (toUser.forall(_.nonEmpty) && toUser.exists(text => !text.startsWith("@"))),
        "ToUser must not be empty and not start with '@'.")

  require(retweetsOfUser.isEmpty || (retweetsOfUser.forall(_.nonEmpty) && retweetsOfUser.exists(text => !text.startsWith("@"))),
        "RetweetOfUser must not be empty and not start with '@'.")

  // TODO validate context, entity, conversationID

  // Rule Options validation
  require(hashtags.isEmpty || (hashtags.forall(_.nonEmpty) && options.forall(options => options.hasHashtags.contains(true))),
        "In order to match Tweets that contain Hashtag, The Option hasHashtags must be enabled.")

  // TODO: validate all properties, all true for testing purpose
  def isValidate: Boolean = true

  private def atLeastOne(): Boolean =
    keyword.nonEmpty || emoji.nonEmpty || mentionedUserId.nonEmpty || phrase.nonEmpty ||
      hashtags.nonEmpty || url.nonEmpty || fromUser.nonEmpty || toUser.nonEmpty ||
      retweetsOfUser.nonEmpty || context.nonEmpty || entity.nonEmpty || conversationId.nonEmpty

  // starting point to build a PayloadEntry, note that PayloadEntry requires a non empty value
  def toBasicPayload: PayloadEntry = PayloadEntry(value = " ")

  private def applyKeyword(payload: PayloadEntry): PayloadEntry = {
    keyword match {
      case Some(keyword: String) =>
        payload.applyKeyword(keyword).group()
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

  private def applyMentionedUserId(payload: PayloadEntry): PayloadEntry = {
    mentionedUserId match {
      case Some(mentionedUserId: String) => payload.applyUserId(mentionedUserId)
      case _ => payload
    }
  }

  private def applyPhrase(payload: PayloadEntry): PayloadEntry = {
    phrase match {
      case Some(phrase: String) => payload.applyPhrase(phrase)
      case _ => payload
    }
  }

  private def applyHashTags(payload: PayloadEntry): PayloadEntry = {
    hashtags match {
      case Some(hashtags: String) => payload.applyHashtag(hashtags)
      case _ => payload
    }
  }

  private def applyUrl(payload: PayloadEntry): PayloadEntry = {
    url match {
      case Some(url: String) => payload.applyUrl(url)
      case _ => payload
    }
  }

  private def applyFromUser(payload: PayloadEntry): PayloadEntry = {
    fromUser match {
      case Some(fromUser: String) => payload.applyFromUser(fromUser)
      case _ => payload
    }
  }

  private def applyToUser(payload: PayloadEntry): PayloadEntry = {
    toUser match {
      case Some(toUser: String) => payload.applyToUser(toUser)
      case _ => payload
    }
  }

  private def applyRetweetsOfUser(payload: PayloadEntry): PayloadEntry = {
    retweetsOfUser match {
      case Some(retweetsOfUser: String) => payload.applyRetweetsOfUser(retweetsOfUser)
      case _ => payload
    }
  }

  private def applyContext(payload: PayloadEntry): PayloadEntry = {
    context match {
      case Some(context: String) => payload.applyContext(context)
      case _ => payload
    }
  }

  private def applyEntity(payload: PayloadEntry): PayloadEntry = {
    entity match {
      case Some(entity: String) => payload.applyEntity(entity)
      case _ => payload
    }
  }

  private def applyConversationId(payload: PayloadEntry): PayloadEntry = {
    conversationId match {
      case Some(conversationId: String) => payload.applyConversationId(conversationId)
      case _ => payload
    }
  }

  private def toPayloadInternal: PayloadEntry = {
    toBasicPayload
      .flatMap(payload => applyKeyword(payload))
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

  def toPayload: PayloadEntry = {
    val result = toPayloadInternal
    val ruleOptions: RuleOptions = options match {
      case Some(ruleOptions: RuleOptions) => ruleOptions
      case _ => RuleOptions.apply()
    }
    result.flatMap(payload => ruleOptions.applyOptions(payload))
  }
}

case object Rule {
  implicit val RuleHandler: BSONDocumentHandler[Rule] = Macros.handler[Rule]
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object BsonObjectIDFormat extends RootJsonFormat[BSONObjectID] {
    override def read(json: JsValue): BSONObjectID =
      json match {
        case JsString(oid) => BSONObjectID.parse(oid) match {
          case Success(parsedObjectId) => parsedObjectId
          case Failure(_)              => deserializationError("BSONObjectID could not be created from given string")
        }
        case _ => throw DeserializationException("ObjectID could not be converted to BSONObjectID object")
      }

    override def write(obj: BSONObjectID): JsValue = JsString(JSONParser.toJson(obj))
  }

  implicit val ruleOptionsFormat: RootJsonFormat[RuleOptions] = jsonFormat10(RuleOptions.apply)
  implicit val fullRuleFormat: RootJsonFormat[Rule] = jsonFormat14(Rule.apply)
}