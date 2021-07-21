package tweets

import org.apache.spark.sql.Row
import reactivemongo.api.bson._
import reactivemongo.api.bson.{BSONReader, BSONWriter, Macros}
import utils.JSONParser

import scala.language.postfixOps

case class Domain(id: String, name: String, description: String)
case object Domain { val domain: BSONDocumentHandler[Domain] = Macros.handler[Domain] }

case class Entity(id: String, name: String, description: String)
case object Entity { val entity: BSONDocumentHandler[Entity] = Macros.handler[Entity] }

/*
  Entity recognition/extraction, topical analysis
 */
case class Context(domain: Domain, entity: Entity)
case object Context {
  implicit val domain: BSONDocumentHandler[Domain] = Macros.handler[Domain]
  implicit val entity: BSONDocumentHandler[Entity] = Macros.handler[Entity]

  val contextHandler: BSONHandler[Context] = Macros.handler[Context]

  val contextSeqReader: BSONReader[Seq[Context]] = BSONReader.iterable[Context, Seq](contextHandler readTry)
  val contextSeqWriter: BSONWriter[Seq[Context]] = BSONWriter.sequence[Context](contextHandler writeTry)
}

/*
  represents user metrics and contains details about activity for this user
 */
case class UserMetrics(followersCount: Int, followingCount: Int, tweetCount: Int, listedCount: Int)

case object UserMetrics {
  implicit val userMetrics: BSONDocumentHandler[UserMetrics] = Macros.handler[UserMetrics]
}

/*
  represents a User
  The root json data only contains an Author Id without any additional user data (see Class User)
  Although all related user's full data are included within the "includes" object
 */
case class User(id:               String,                     // The unique identifier of this user
                name:             String,                     // The name of the user, as they’ve defined it on their profile
                username:         String,                     // The Twitter screen name, handle, or alias that this user identifies themselves with
                createdAt:        String,                     // The UTC datetime that the user account was created on Twitter
                description:      Option[String]      =None,  // The text of this user's profile description (also known as bio), if the user provided one
                location:         Option[String]      =None,  // The location specified in the user's profile, if the user provided one
                profileImageUrl:  Option[String]      =None,  // The URL to the profile image for this user, as shown on the user's profile
                metrics:          Option[UserMetrics] =None,  // Contains details about activity for this user, NOTE null for now
                url:              Option[String]      =None,  // The URL specified in the user's profile, if present
                verified:         Boolean                     // Indicates if this user is a verified Twitter User
               )

case object User {
  val userHandler: BSONHandler[User] = Macros.handler[User]

  val userSeqReader: BSONReader[Seq[User]] = BSONReader.iterable[User, Seq](userHandler readTry)
  val userSeqWriter: BSONWriter[Seq[User]] = BSONWriter.sequence[User](userHandler writeTry)
}

/*
  represents a URL, expandedUrl and displayUrl are optional
 */
case class Url(url: String, expandedUrl: String="", displayUrl: String="")
case object Url {
  val urlHandler: BSONDocumentHandler[Url] = Macros.handler[Url]

  val urlSeqReader: BSONReader[Seq[Url]] = BSONReader.iterable[Url, Seq](urlHandler readTry)
  val urlSeqWriter: BSONWriter[Seq[Url]] = BSONWriter.sequence[Url](urlHandler writeTry)
}

/*
  Entities are JSON objects that provide additional information about hashtags, urls, user mentions, and cashtags associated with a Tweet
  we utilise hashtags, mentionedUsers, mentionedUrls for now
 */
case class Entities(hashtags: Option[Seq[String]]=None, mentionedUrls: Option[Seq[Url]]=None)

case object Entities {
  implicit val mentionedUrlHandler: BSONDocumentHandler[Url] = Macros.handler[Url]

  val entitiesHandler: BSONHandler[Entities] = Macros.handler[Entities]

  val entitiesSeqReader: BSONReader[Seq[Entities]] = BSONReader.iterable[Entities, Seq](entitiesHandler readTry)
  val entitiesSeqWriter: BSONWriter[Seq[Entities]] = BSONWriter.sequence[Entities](entitiesHandler writeTry)
}

/*
 	Public engagement metrics for the Tweet at the time of the request, use this to measure Tweet engagement
 */
case class PublicMetrics(retweetCount: Int, replyCount: Int, likeCount: Int, quoteCount: Int)

case object PublicMetrics {
  implicit val publicMetricsHandler: BSONDocumentHandler[PublicMetrics] = Macros.handler[PublicMetrics]
}

case class NonPublicMetrics(impressionCount: Int, urlLinkClicks: Int, userProfileClicks: Int)

case object NonPublicMetrics {
  implicit val nonPublicMetricsHandler: BSONDocumentHandler[NonPublicMetrics] = Macros.handler[NonPublicMetrics]
}

/*
 A help class to avoid abusing the class "Rule" which in this case will only contains id and text,
 the other 18 attributes are unset.
 */
case class MatchingRule(id: String, tag: String)

case object MatchingRule {
  implicit val matchingRuleHandler: BSONHandler[MatchingRule] = Macros.handler[MatchingRule]
}

/*
  represents a Tweet object retrieved from the Twitter API, which contains only id and text as default fields
  Although it would make no sense only to provide id and text for further processing because this Tweet object
  is intended for data analyse purpose, we do not have the ability to guarantee neither the format of the data,
  nor the completeness of the data coming from the Twitter API.
 */
// TODO Add possibly sensitive and maybe also geo location
case class Tweet(
                  id:                String,                          // The unique identifier of the requested Tweet
                  text:              String,                          // The actual UTF-8 text of the Tweet
                  createdAt:         Option[String]           =None,  // Creation time of the Tweet
                  author:            Option[User]             =None,  // The Author who posted this Tweet
                  inReplyToUserId:   Option[String]           =None,  // If the represented Tweet is a reply, this field will contain the original Tweet’s author ID
                  publicMetrics:     Option[PublicMetrics]    =None,  // Public engagement metrics for the Tweet at the time of the request
                  nonPublicMetrics:  Option[NonPublicMetrics] =None,  // Non-public engagement metrics for the Tweet at the time of the request
                  context:           Option[Seq[Context]]     =None,  // Contains context annotations for the Tweet
                  entities:          Option[Seq[Entities]]    =None,  // Entities which have been parsed out of the text of the Tweet
                  mentionedUsers:    Option[Seq[User]]        =None,  // Users this tweet has mentioned in the body text
                  matchingRules:     Option[Seq[MatchingRule]]=None,  // annotations about which filtered Rule this tweet was matched with
                  conversationId:    Option[String]           =None,  // The Tweet ID of the original Tweet of the conversation (which includes direct replies, replies of replies).
                  source:            Option[String]           =None,  // The name of the app the user Tweeted from
                  lang:              Option[String]           =None   // Language of the Tweet, if detected by Twitter. Returned as a BCP47 language tag
                )
{
  def map(transformer: tweets.Tweet => tweets.Tweet): tweets.Tweet = {
    transformer(this)
  }
}

case object Tweet {
  /*
    build a Tweet Object form the JSON Object contains data, includes, matching_rules as object
    {
      "data": {},
      "includes": {},
      "matching_rules": {}
    }
   */
  def createTweet(json: String): Option[Tweet] = {
    JSONParser.parseJson(json) match {
      case Some(jsonMap: Map[String, Any]) =>
        val dataMap: Map[String, Any] = jsonMap.getOrElse("data", Map()).asInstanceOf[Map[String, Any]]
        val basicTweet = this.buildBasicTweet(dataMap)
        val withDataProperties = if (dataMap.nonEmpty) {
          this.applyDataProperties(basicTweet, dataMap)
        } else {
          basicTweet
        }
        val withUsers = this.applyUsers(withDataProperties, jsonMap)
        val ruleMap: List[Map[String, Any]] = jsonMap.getOrElse("matching_rules", List()).asInstanceOf[List[Map[String, Any]]]
        if (ruleMap.nonEmpty) {
          this.applyMatchingRules(withUsers, ruleMap)
        } else {
          withUsers
        }
      case _ => None
    }
  }

  /*
    build a basic Tweet object using attributes obtained within the data object
    attributes: id, text, createdAt, InReplyToUserId, ConversationId, Source, lang
   */
  def buildBasicTweet(dataMap: Map[String, Any]): Option[Tweet] = {
    val tweet = (dataMap.get("id"), dataMap.get("text")) match {
      case (Some(id: String), Some(text: String)) =>
        Some(Tweet(id=id, text=text))
      case _ => None
    }
    tweet
      .flatMap(tweet => applyCreatedAt(tweet, dataMap))
      .flatMap(tweet => applyReplyToUserId(tweet, dataMap))
      .flatMap(tweet => applyConversationId(tweet, dataMap))
      .flatMap(tweet => applySource(tweet, dataMap))
      .flatMap(tweet => applyLanguage(tweet, dataMap))
  }

  /*
    apply non basic attributes to the basic Tweet object which include metrics, context, entities
    within the data object
   */
  def applyDataProperties(tweet: Option[Tweet], dataMap: Map[String, Any]): Option[Tweet] = {
    tweet
      .flatMap(tweet => applyMetrics(tweet, dataMap))
      .flatMap(tweet => applyContext(tweet, dataMap))
      .flatMap(tweet => applyEntities(tweet, dataMap))
  }

  /*
    extract author as a User object, and mentionedUsers as a list of users
    the author, if presents, would be extracted from the mentioned user list
   */
  def applyUsers(tweet: Option[Tweet], jsonMap: Map[String, Any]): Option[Tweet] = {
    val includesMap: Map[String, Any] = jsonMap.getOrElse("includes", Map()).asInstanceOf[Map[String, Any]]
    tweet
      .flatMap(tweet => applyAuthor(tweet, jsonMap))
      .flatMap(tweet => applyMentionedUsers(tweet, includesMap))
  }

  // if creation date is not presents, return the given tweet object wrapped with optional
  private def applyCreatedAt(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
     dataMap.get("created_at") match {
      case Some(createdAt: String) =>
        Some(Tweet(id = tweet.id, text = tweet.text, createdAt = Some(createdAt)))
      case _ => Some(tweet)
    }
  }

  // if the user id this tweet was replied to is not presents, return the given tweet object wrapped with optional
  private def applyReplyToUserId(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("in_reply_to_user_id") match {
      case Some(replyToUserId: String) =>
        Some(Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = Some(replyToUserId)
        ))
      case _ => Some(tweet)
    }
  }

  // if conversation id is not presents, return the given tweet object wrapped with optional
  private def applyConversationId(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("conversation_id") match {
      case Some(conversationId: String) =>
        Some(Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = tweet.inReplyToUserId,
          conversationId = Some(conversationId)
        ))
      case _ => Some(tweet)
    }
  }

  // if the source is not presents, return the given tweet object wrapped with optional
  private def applySource(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("source") match {
      case Some(source: String) =>
        Some(Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = tweet.inReplyToUserId,
          conversationId = tweet.conversationId,
          source = Some(source)
        ))
      case _ => Some(tweet)
    }
  }

  // if the language is not presents, return the given tweet object wrapped with optional
  private def applyLanguage(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("lang") match {
      case Some(lang: String) =>
        Some(Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = tweet.inReplyToUserId,
          conversationId = tweet.conversationId,
          source = tweet.source,
          lang = Some(lang)
        ))
      case _ => Some(tweet)
    }
  }

  /*
    apply both public and non public metrics for the tweet if presents
   */
  def applyMetrics(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    val publicMetrics: Map[String, Any] = dataMap.getOrElse("public_metrics", Map()).asInstanceOf[Map[String, Any]]
    val nonPublicMetrics: Map[String, Any] = dataMap.getOrElse("non_public_metrics", Map()).asInstanceOf[Map[String, Any]]
    val withPublicMetrics = if (publicMetrics.nonEmpty) {
      applyPublicMetrics(tweet, publicMetrics)
    } else {
      Some(tweet)
    }
    if (nonPublicMetrics.nonEmpty) {
      applyNonPublicMetrics(tweet, nonPublicMetrics)
    } else {
      withPublicMetrics
    }
  }

  def applyPublicMetrics(tweet: Tweet, metrics: Map[String, Any]): Option[Tweet] = {
    Some(Tweet(
      id=tweet.id,
      text=tweet.text,
      createdAt=tweet.createdAt,
      author=tweet.author,
      inReplyToUserId=tweet.inReplyToUserId,
      publicMetrics=extractPublicMetrics(metrics),
      nonPublicMetrics=tweet.nonPublicMetrics,
      context=tweet.context,
      entities=tweet.entities,
      mentionedUsers=tweet.mentionedUsers,
      matchingRules=tweet.matchingRules,
      conversationId=tweet.conversationId,
      source=tweet.source,
      lang=tweet.lang
    ))
  }

  def applyNonPublicMetrics(tweet: Tweet, metrics: Map[String, Any]): Option[Tweet] = {
    Some(Tweet(
      id=tweet.id,
      text=tweet.text,
      createdAt=tweet.createdAt,
      author=tweet.author,
      inReplyToUserId=tweet.inReplyToUserId,
      publicMetrics=tweet.publicMetrics,
      nonPublicMetrics=extractNonPublicMetrics(metrics),
      context=tweet.context,
      entities=tweet.entities,
      mentionedUsers=tweet.mentionedUsers,
      matchingRules=tweet.matchingRules,
      conversationId=tweet.conversationId,
      source=tweet.source,
      lang=tweet.lang
    ))
  }

  def extractPublicMetrics(metrics: Map[String, Any]): Option[PublicMetrics] = {
    Some(PublicMetrics(
      retweetCount = metrics.getOrElse("retweet_count", 0).asInstanceOf[Int],
      replyCount = metrics.getOrElse("reply_count", 0).asInstanceOf[Int],
      likeCount = metrics.getOrElse("like_count", 0).asInstanceOf[Int],
      quoteCount = metrics.getOrElse("quote_count", 0).asInstanceOf[Int]
    ))
  }

  def extractNonPublicMetrics(metrics: Map[String, Any]): Option[NonPublicMetrics] = {
    Some(NonPublicMetrics(
      impressionCount = metrics.getOrElse("impression_count", 0).asInstanceOf[Int],
      urlLinkClicks = metrics.getOrElse("url_link_clicks", 0).asInstanceOf[Int],
      userProfileClicks = metrics.getOrElse("user_profile_clicks", 0).asInstanceOf[Int]
    ))
  }

  def applyContext(tweet: Tweet, data: Map[String, Any]): Option[Tweet] = {
    val context: Option[Seq[Context]] = extractContext(data.getOrElse("context_annotations", List()).asInstanceOf[List[Map[String, Any]]])
    context match {
      case Some(_: Seq[Context]) =>
        Some(Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          publicMetrics=tweet.publicMetrics,
          nonPublicMetrics=tweet.nonPublicMetrics,
          context=context,
          entities=tweet.entities,
          mentionedUsers=tweet.mentionedUsers,
          matchingRules=tweet.matchingRules,
          conversationId=tweet.conversationId,
          source=tweet.source,
          lang=tweet.lang
        ))
      case _ => Some(tweet)
    }
  }

  def applyEntities(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    val entities = extractEntities(dataMap.getOrElse("entities", Map()).asInstanceOf[Map[String, Any]])
    entities match {
      case Some(_: Seq[Entities]) =>
        Some(Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          publicMetrics=tweet.publicMetrics,
          nonPublicMetrics=tweet.nonPublicMetrics,
          context=tweet.context,
          entities=entities,
          mentionedUsers=tweet.mentionedUsers,
          matchingRules=tweet.matchingRules,
          conversationId=tweet.conversationId,
          source=tweet.source,
          lang=tweet.lang
        ))
      case _ => Some(tweet)
    }
  }

  def applyAuthor(tweet: Tweet, jsonMap: Map[String, Any]): Option[Tweet] = {
    val dataMap: Map[String, Any] = jsonMap.getOrElse("data", Map()).asInstanceOf[Map[String, Any]]
    val userMap: List[Map[String, Any]] = jsonMap
      .getOrElse("includes", Map()).asInstanceOf[Map[String, Any]]
      .getOrElse("users", List()).asInstanceOf[List[Map[String, Any]]]
    dataMap.get("author_id") match {
      case Some(authorId: String) =>
        val author = extractAuthor(authorId, userMap)
        Some(Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          author = author,
          inReplyToUserId = tweet.inReplyToUserId,
          publicMetrics = tweet.publicMetrics,
          nonPublicMetrics = tweet.nonPublicMetrics,
          context = tweet.context,
          entities = tweet.entities,
          mentionedUsers = tweet.mentionedUsers,
          matchingRules = tweet.matchingRules,
          source = tweet.source,
          conversationId = tweet.conversationId,
          lang = tweet.lang
        ))
      case _ => Some(tweet)
    }
  }

  def extractAuthor(authorId: String, userMap: List[Map[String, Any]]): Option[User] = {
    val users: Option[Seq[User]] = extractUsers(userMap)
    users match {
      case Some(users: Seq[User]) =>
        Some(users.filter(u => authorId.equals(u.id)).head)
      case _ => None
    }
  }

  def applyMentionedUsers(tweet: Tweet, includesMap: Map[String, Any]): Option[Tweet] = {
    val userMap: List[Map[String, Any]] = includesMap.getOrElse("users", List()).asInstanceOf[List[Map[String, Any]]]
    if (userMap.nonEmpty) {
      val mentionedUsers = extractMentionedUsers(tweet.author, extractUsers(userMap))
      Some(Tweet(
        id=tweet.id,
        text=tweet.text,
        createdAt=tweet.createdAt,
        author=tweet.author,
        inReplyToUserId=tweet.inReplyToUserId,
        publicMetrics=tweet.publicMetrics,
        nonPublicMetrics=tweet.nonPublicMetrics,
        context=tweet.context,
        entities=tweet.entities,
        mentionedUsers=mentionedUsers,
        matchingRules=tweet.matchingRules,
        conversationId=tweet.conversationId,
        source=tweet.source,
        lang=tweet.lang
      ))
    } else {
      Some(tweet)
    }
  }

  def applyMatchingRules(tweet: Option[Tweet], ruleMap: List[Map[String, Any]]): Option[Tweet] = {
    extractRules(ruleMap) match {
      case Some(rules: Seq[MatchingRule]) =>
        Some(Tweet(
          id=tweet.get.id,
          text=tweet.get.text,
          createdAt=tweet.get.createdAt,
          author=tweet.get.author,
          inReplyToUserId=tweet.get.inReplyToUserId,
          publicMetrics=tweet.get.publicMetrics,
          nonPublicMetrics=tweet.get.nonPublicMetrics,
          context=tweet.get.context,
          entities=tweet.get.entities,
          matchingRules=Some(rules),
          conversationId=tweet.get.conversationId,
          source=tweet.get.source,
          lang=tweet.get.lang))
      case _ => tweet
    }
  }

  // Raw data might contain duplicated context annotations
  def extractContext(context: List[Map[String, Any]]): Option[Seq[Context]] = {
    if (context.nonEmpty) {
      Some(context.flatMap(context => {
        val domain = extractDomain(context("domain").asInstanceOf[Map[String, Any]])
        val entity = extractEntity(context("entity").asInstanceOf[Map[String, Any]])
        List(Context(domain = domain, entity = entity))
      }))
    } else {
      None
    }
  }

  def extractDomain(domain: Map[String, Any]): Domain = {
    Domain(
      id = domain.getOrElse("id", "").asInstanceOf[String],
      name = domain.getOrElse("name", "").asInstanceOf[String],
      description = domain.getOrElse("description", "").asInstanceOf[String]
    )
  }

  def extractEntity(entity: Map[String, Any]): Entity = {
    Entity(
      id = entity.getOrElse("id", "").asInstanceOf[String],
      name = entity.getOrElse("name", "").asInstanceOf[String],
      description = entity.getOrElse("description", "").asInstanceOf[String]
    )
  }

  // Raw data might contain duplicated context annotations
  def extractEntities(entities: Map[String, Any]): Option[Seq[Entities]] = {
    if (entities.nonEmpty) {
      val urls = extractMentionedUrls(entities.getOrElse("urls", List()).asInstanceOf[List[Map[String, Any]]])
      val hashtags = extractHashtags(entities.getOrElse("hashtags", List()).asInstanceOf[List[Map[String, Any]]])
      // entities object could contain mentions (users), hashtags, urls, cashtags ans other attributes
      // even if entities object is not empty, urls and hashtags could still be empty
      if (urls.isEmpty && hashtags.isEmpty) {
        None
      } else {
        Some(List(Entities(mentionedUrls = urls, hashtags = hashtags)))
      }
    } else {
      None
    }
  }

  // extract all users within "includes" data map, contains both author and mentioned users
  def extractUsers(userMap: List[Map[String, Any]]): Option[Seq[User]] = {
    if (userMap.nonEmpty) {
      Some(
        userMap.flatMap(user => List(
          User(
            id = user.getOrElse("id", "").asInstanceOf[String],
            name = user.getOrElse("name", "").asInstanceOf[String],
            username = user.getOrElse("username", "").asInstanceOf[String],
            createdAt = user.getOrElse("created_at", "").asInstanceOf[String],
            description = user.get("description").asInstanceOf[Option[String]],
            location = user.get("location").asInstanceOf[Option[String]],
            profileImageUrl = user.get("profile_image_url").asInstanceOf[Option[String]],
            metrics = extractUserMetrics(user.getOrElse("public_metrics", Map()).asInstanceOf[Map[String, Any]]),
            url = user.get("url").asInstanceOf[Option[String]],
            verified = user.getOrElse("verified", false).asInstanceOf[Boolean])
          )
        )
      )
    } else {
      None
    }
  }

  def extractUserMetrics(metrics: Map[String, Any]): Option[UserMetrics] = {
    if (metrics.nonEmpty) {
      Some(
        UserMetrics(
          followersCount = metrics.getOrElse("followers_count", 0).asInstanceOf[Int],
          followingCount = metrics.getOrElse("following_count", 0).asInstanceOf[Int],
          tweetCount = metrics.getOrElse("tweet_count", 0).asInstanceOf[Int],
          listedCount = metrics.getOrElse("listed_count", 0).asInstanceOf[Int]
        )
      )
    } else {
      None
    }
  }

  // filter out the author, if it is presents, return the given users otherwise
  def extractMentionedUsers(author: Option[User]=None, users: Option[Seq[User]]): Option[Seq[User]] = {
    (author, users) match {
      case (Some(author: User), Some(users: Seq[User])) =>
        Some(users.filter(user => !author.id.equals(user.id)))
      case (None, Some(users: Seq[User])) =>
        Some(users)
      case (_, _) => users
    }
  }

  def extractMentionedUrls(urlList: List[Map[String, Any]]): Option[Seq[Url]] = {
    if (urlList.nonEmpty) {
      Some(
        urlList.flatMap(url => List(
            Url(
              url = url.getOrElse("url", "").asInstanceOf[String],
              expandedUrl = url.getOrElse("expanded_url", "").asInstanceOf[String],
              displayUrl = url.getOrElse("display_url", "").asInstanceOf[String],
            )
          )
        )
      )
    } else {
      None
    }
  }

  def extractHashtags(hashtags: List[Map[String,Any]]): Option[Seq[String]] ={
    if (hashtags.nonEmpty) {
      Some(
        hashtags.flatMap(tag => {
          val t = tag.getOrElse("tag", "").asInstanceOf[String]; if (t == "") List() else List(t)
        })
      )
    } else {
      None
    }
  }

  def extractRules(ruleList: List[Map[String, Any]]): Option[Seq[MatchingRule]] = {
    if (ruleList.nonEmpty) {
      Some(
        ruleList.flatMap(rule => {
          val id = rule.getOrElse("id", "").toString
          val tag = rule.getOrElse("tag", "").asInstanceOf[String]
          if (id != "" && tag != "") List(MatchingRule(id, tag)) else List()
        })
      )
    } else {
      None
    }
  }

  def createTweetFromRow(row: Row): Tweet = {
    Tweet(
      id = row.getString(0),
      text = row.getString(1),
      createdAt = Some(row.getString(2)),
      author = createUser(Some(row.get(3).asInstanceOf[Row])),
      inReplyToUserId = Some(row.get(4).asInstanceOf[String]),
      publicMetrics = createPublicMetrics(Some(row.get(5).asInstanceOf[Row])),
      nonPublicMetrics = createNonPublicMetrics(Some(row.get(6).asInstanceOf[Row])),
      context = createContext(Some(row.getSeq[Row](7))),
      entities = createEntities(Some(row.getSeq[Row](8))),
      mentionedUsers = createMentionedUsers(Some(row.getSeq[Row](9))),
      matchingRules = createMatchingRules(Some(row.getSeq[Row](10))),
      conversationId = Some(row.get(11).asInstanceOf[String]),
      source = Some(row.get(12).asInstanceOf[String]),
      lang = Some(row.get(13).asInstanceOf[String])
    )
  }

  def createUser(row: Option[Row]): Option[User] = {
    row match {
      case Some(value: Row) =>
        Some(User(
          id = value.getString(0),
          name = value.getString(1),
          username = value.getString(2),
          createdAt = value.getString(3),
          description = Some(value.get(4).asInstanceOf[String]),
          location = Some(value.get(5).asInstanceOf[String]),
          profileImageUrl = Some(value.get(6).asInstanceOf[String]),
          metrics = createUserMetrics(Some(value.get(7).asInstanceOf[Row])),
          url = Some(value.get(8).asInstanceOf[String]),
          verified =value.getBoolean(9)
        ))
      case _ => None
    }
  }

  def createPublicMetrics(row: Option[Row]): Option[PublicMetrics] = {
    row match {
      case Some(value: Row) =>
        Some(PublicMetrics(
          retweetCount = value.getInt(0),
          replyCount = value.getInt(1),
          likeCount = value.getInt(2),
          quoteCount = value.getInt(3)
        ))
      case _ => None
    }
  }

  def createNonPublicMetrics(row: Option[Row]): Option[NonPublicMetrics] = {
    row match {
      case Some(value: Row) =>
        Some(NonPublicMetrics(
          impressionCount = value.getInt(0),
          urlLinkClicks = value.getInt(1),
          userProfileClicks = value.getInt(2)
        ))
      case _ => None
    }
  }

  // TODO deduplicate context
  def createContext(rows: Option[Seq[Row]]): Option[Seq[Context]] = {
    rows match {
      case Some(values: Seq[Row]) =>
        Some(values.map(context => {
          val domainRow = context.get(0).asInstanceOf[Row]
          val entityRow = context.get(1).asInstanceOf[Row]
          Context(
            domain = Domain(id = domainRow.getString(0), name = domainRow.getString(1), description = domainRow.getString(2)),
            entity = Entity(id = entityRow.getString(0), name = entityRow.getString(1), description = entityRow.getString(2))
          )
        }))
      case _ => None
    }
  }

  // TODO deduplicate entities
  def createEntities(rows: Option[Seq[Row]]): Option[Seq[Entities]] =
  {
    rows match {
      case Some(values: Seq[Row]) =>
        Some(values.map(entity => {
          val hashtags: Option[Seq[String]] = Some(entity.getSeq[String](0))
          val urlRows: Option[Seq[Row]] = Some(entity.getSeq[Row](1))
          val urls: Option[Seq[Url]] = urlRows match {
            case Some(rows: Seq[Row]) =>
              Some(rows.map(row =>
                Url(url = row.getString(0), expandedUrl = row.getString(1), displayUrl = row.getString(2))
              ))
            case _ => None
          }
          Entities(hashtags = hashtags, mentionedUrls = urls)
        }))
      case _ => None
    }
  }

  def createMentionedUsers(rows: Option[Seq[Row]]): Option[Seq[User]] = {
    rows match {
      case Some(values: Seq[Row]) =>
        Some(values.map(userRow => createUser(Some(userRow)).get))
      case _ => None
    }
  }

  def createUserMetrics(row: Option[Row]): Option[UserMetrics] = {
    row match {
      case Some(value: Row) =>
        Some(UserMetrics(
          followersCount = value.getInt(0),
          followingCount = value.getInt(1),
          tweetCount = value.getInt(2),
          listedCount = value.getInt(3)
        ))
      case _ => None
    }
  }

  def createMatchingRules(row: Option[Seq[Row]]): Option[Seq[MatchingRule]] = {
    row match {
      case Some(value: Seq[Row]) =>
        Some(value.map(rule => MatchingRule(id = rule.getString(0), tag = rule.getString(1))))
      case _ => None
    }
  }

  implicit val context:        BSONDocumentHandler[Context]  = Macros.handler[Context]
  implicit val entity :        BSONDocumentHandler[Entity]   = Macros.handler[Entity]
  implicit val domain:         BSONDocumentHandler[Domain]   = Macros.handler[Domain]
  implicit val mentionedUsers: BSONDocumentHandler[User]     = Macros.handler[User]
  implicit val mentionedUrls:  BSONDocumentHandler[Url]      = Macros.handler[Url]
  implicit val entities:       BSONDocumentHandler[Entities] = Macros.handler[Entities]
  implicit val TweetHandler:   BSONDocumentHandler[Tweet]    = Macros.handler[Tweet]
}
