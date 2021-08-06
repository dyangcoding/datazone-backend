package rules

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import utils.StringUtils

case class RuleOptions(isRetweet:    Option[Boolean]=None, // match Tweets that are truly retweets
                       isVerified:   Option[Boolean]=None, // deliver only Tweets whose authors are verified by Twitter
                       isReply:      Option[Boolean]=None, // deliver only explicit replies that match a rule
                       hasHashtags:  Option[Boolean]=None, // match Tweets that contain at least one hashtag
                       hasLinks:     Option[Boolean]=None, // match Tweets which contain links and media in the Tweet body
                       hasMedia:     Option[Boolean]=None, // match Tweets that contain a media object, such as a photo, GIF, or video, as determined by Twitter
                       hasImages:    Option[Boolean]=None, // match Tweets that contain a recognized URL to an image
                       hasVideos:    Option[Boolean]=None, // match Tweets that contain native Twitter videos, uploaded directly to Twitter
                       lang:         Option[String] =None, // match Tweets that have been classified by Twitter as being of a particular language
                       sample:       Option[Int]    =None  // a random percent sample of Tweets that match a rule rather than the entire set of Tweets
                      ) {

  require(sample.isEmpty || sample.forall(sample => sample > 0 && sample <= 100),
    "Sample must be within the 0 (exclusive) and 100 (inclusive)")

  require(lang.isEmpty || (lang.forall(_.nonEmpty) && StringUtils.langList().contains(lang.toString)),
    "Language must be one of the currently supported 47 Languages")
}

case object RuleOptions {
  implicit val RuleOptionHandler: BSONDocumentHandler[RuleOptions] = Macros.handler[RuleOptions]
}

case class Rule(
                id:              Option[String]      =None, // will be generated once it is verified by the Twitter API and could be utilised to deduplicate objects within DB
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
                tag:             Option[String]      =None, // could be utilised for sorting rule, for now just a simple string
                options:         Option[RuleOptions] =None
){
  // prohibit client sending empty Rule Data, also Rule Options can not be utilised alone for building rules
  // require(atLeastOne(), "Provide at least One possible Rule Operator to effectively match any Tweets.")

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

  private def atLeastOne(): Boolean =
    keyword.nonEmpty || emoji.nonEmpty || mentionedUserId.nonEmpty || phrase.nonEmpty ||
      hashtags.nonEmpty || url.nonEmpty || fromUser.nonEmpty || toUser.nonEmpty ||
      retweetsOfUser.nonEmpty || context.nonEmpty || entity.nonEmpty || conversationId.nonEmpty
}

case object Rule {
  implicit val RuleHandler: BSONDocumentHandler[Rule] = Macros.handler[Rule]
}

// provides Json Unmarshalling utility
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ruleOptionsFormat: RootJsonFormat[RuleOptions] = jsonFormat10(RuleOptions.apply)
  implicit val fullRuleFormat: RootJsonFormat[Rule] = jsonFormat15(Rule.apply)
}