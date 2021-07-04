package rules

import reactivemongo.api.bson.{BSONDocumentHandler, Macros}
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

  require(lang.isEmpty || (lang.forall(_.nonEmpty) && !StringUtils.langList().contains(lang.toString)),
    "Language must be one of the currently supported 47 Languages")

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

  private def applyLanguage(payload: PayloadEntry): PayloadEntry = {
    lang match {
      case Some(lang: String) => payload.applyLanguage(lang)
      case _ => payload.applyLanguage("en")
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
      .flatMap(payload => applyLanguage(payload))
      .flatMap(payload => applySample(payload))
  }
}

case object RuleOptions {
  implicit val RuleOptionHandler: BSONDocumentHandler[RuleOptions] = Macros.handler[RuleOptions]
}
