package tweets

import org.apache.spark.sql.Row
import reactivemongo.api.bson._
import reactivemongo.api.bson.{BSONReader, BSONWriter, Macros}
import utils.JSONParser

import scala.language.postfixOps

case class Domain(id: String, name: String, description: String)
case object Domain {
  val domainHandler: BSONDocumentHandler[Domain] = Macros.handler[Domain]

  val domainSeqReader: BSONReader[Seq[Domain]] = BSONReader.iterable[Domain, Seq](domainHandler readTry)
  val domainSeqWriter: BSONWriter[Seq[Domain]] = BSONWriter.sequence[Domain](domainHandler writeTry)
}

case class Entity(id: String, name: String, description: String)
case object Entity {
  val entityHandler: BSONDocumentHandler[Entity] = Macros.handler[Entity]

  val entitySeqReader: BSONReader[Seq[Entity]] = BSONReader.iterable[Entity, Seq](entityHandler readTry)
  val entitySeqWriter: BSONWriter[Seq[Entity]] = BSONWriter.sequence[Entity](entityHandler writeTry)
}

/*
  Entity recognition/extraction, topical analysis
 */
case class Context(domain: Option[Seq[Domain]]=None, entity: Option[Seq[Entity]])
case object Context {
  implicit val domain: BSONDocumentHandler[Domain] = Macros.handler[Domain]
  implicit val entity: BSONDocumentHandler[Entity] = Macros.handler[Entity]

  val contextHandler: BSONHandler[Context] = Macros.handler[Context]
}

/*
  represents user metrics and contains details about activity for this user
 */
case class UserMetrics(followersCount: Int = 0, followingCount: Int = 0, tweetCount: Int = 0, listedCount: Int = 0) {
  require(followersCount >= 0, "An User can not have negative followers")
  require(followingCount >= 0, "An User can not have negative following Users")
  require(tweetCount     >= 0, "An User can not have negative tweets")
  require(listedCount    >= 0, "An User can at least not be listed")
}

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
}

/*
 	Public engagement metrics for the Tweet at the time of the request, use this to measure Tweet engagement
 */
case class PublicMetrics(retweetCount: Int = 0, replyCount: Int = 0, likeCount: Int = 0, quoteCount: Int = 0) {
  require(retweetCount >= 0, "A Tweet can at least be retweeted zero times")
  require(replyCount   >= 0, "A Tweet can at least have zero replies")
  require(likeCount    >= 0, "A Tweet can at least have zero likes")
  require(quoteCount   >= 0, "A Tweet can at least have zero quotes")
}

case object PublicMetrics {
  implicit val publicMetricsHandler: BSONDocumentHandler[PublicMetrics] = Macros.handler[PublicMetrics]
}

case class NonPublicMetrics(impressionCount: Int = 0, urlLinkClicks: Int = 0, userProfileClicks: Int = 0) {
  require(impressionCount   >= 0, "A Tweet's Impression Count can not be negative")
  require(urlLinkClicks     >= 0, "A Tweet's url link click Count can not be negative")
  require(userProfileClicks >= 0, "User Profile Click Count can not be negative")
}

case object NonPublicMetrics {
  implicit val nonPublicMetricsHandler: BSONDocumentHandler[NonPublicMetrics] = Macros.handler[NonPublicMetrics]
}

/*
 A help class to avoid abusing the class "Rule" which in this case will only contains id and text,
 the other attributes are unset.
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
                  context:           Option[Context]          =None,  // Contains context annotations for the Tweet
                  entities:          Option[Entities]         =None,  // Entities which have been parsed out of the text of the Tweet
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
    build a Tweet Object form the JSON Object contains data, includes, matching_rules as a nested sub object
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
        Some(tweet.copy(createdAt = Some(createdAt)))
      case _ => Some(tweet)
    }
  }

  // if the user id this tweet was replied to is not presents, return the given tweet object wrapped with optional
  private def applyReplyToUserId(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("in_reply_to_user_id") match {
      case Some(replyToUserId: String) =>
        Some(tweet.copy(inReplyToUserId = Some(replyToUserId)))
      case _ => Some(tweet)
    }
  }

  // if conversation id is not presents, return the given tweet object wrapped with optional
  private def applyConversationId(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("conversation_id") match {
      case Some(conversationId: String) =>
        Some(tweet.copy(conversationId = Some(conversationId)))
      case _ => Some(tweet)
    }
  }

  // if the source is not presents, return the given tweet object wrapped with optional
  private def applySource(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("source") match {
      case Some(source: String) =>
        Some(tweet.copy(source = Some(source)))
      case _ => Some(tweet)
    }
  }

  // if the language is not presents, return the given tweet object wrapped with optional
  private def applyLanguage(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    dataMap.get("lang") match {
      case Some(lang: String) =>
        Some(tweet.copy(lang = Some(lang)))
      case _ => Some(tweet)
    }
  }

  /*
    apply both public and non public metrics for the tweet if presents
   */
  def applyMetrics(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    val publicMetrics: Map[String, Any] = dataMap.getOrElse("public_metrics", Map()).asInstanceOf[Map[String, Any]]
    val nonPublicMetrics: Map[String, Any] = dataMap.getOrElse("non_public_metrics", Map()).asInstanceOf[Map[String, Any]]
    val withPublicMetrics = if (publicMetrics.isEmpty) {
      Some(tweet)
    } else {
      applyPublicMetrics(tweet, publicMetrics)
    }
    if (nonPublicMetrics.isEmpty) {
      withPublicMetrics
    } else {
      applyNonPublicMetrics(tweet, nonPublicMetrics)
    }
  }

  def applyPublicMetrics(tweet: Tweet, metrics: Map[String, Any]): Option[Tweet] = {
    Some(tweet.copy(publicMetrics = extractPublicMetrics(metrics)))
  }

  def applyNonPublicMetrics(tweet: Tweet, metrics: Map[String, Any]): Option[Tweet] = {
    Some(tweet.copy(nonPublicMetrics = extractNonPublicMetrics(metrics)))
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
    val context: Option[Context] = extractContext(data.getOrElse("context_annotations", List()).asInstanceOf[List[Map[String, Any]]])
    context match {
      case Some(_: Context) =>
        Some(tweet.copy(context = context))
      case _ => Some(tweet)
    }
  }

  def applyEntities(tweet: Tweet, dataMap: Map[String, Any]): Option[Tweet] = {
    val entities: Option[Entities] = extractEntities(dataMap.getOrElse("entities", Map()).asInstanceOf[Map[String, Any]])
    entities match {
      case Some(_: Entities) =>
        Some(tweet.copy(entities = entities))
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
        Some(tweet.copy(author = author))
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
      Some(tweet.copy(mentionedUsers = mentionedUsers))
    } else {
      Some(tweet)
    }
  }

  def applyMatchingRules(tweet: Option[Tweet], ruleMap: List[Map[String, Any]]): Option[Tweet] = {
    extractRules(ruleMap) match {
      case Some(rules: Seq[MatchingRule]) =>
        tweet.flatMap(tweet => Some(tweet.copy(matchingRules = Some(rules))))
      case _ => tweet
    }
  }

  // Raw data might contain duplicated context annotations
  def extractContext(context: List[Map[String, Any]]): Option[Context] = {
    if (context.isEmpty) {
      None
    } else {
      var domainList: Seq[Domain] = List()
      var entityList: Seq[Entity] = List()
      context.distinct.foreach(context => {
        domainList = domainList :+ extractDomain(context("domain").asInstanceOf[Map[String, Any]])
        entityList = entityList :+ extractEntity(context("entity").asInstanceOf[Map[String, Any]])
      })
      if (domainList.isEmpty && entityList.isEmpty) {
        None
      } else {
        Some(Context(domain = Some(domainList.distinct), entity = Some(entityList.distinct)))
      }
    }
  }

  def extractDomain(domain: Map[String, Any]): Domain = {
    Domain(
      id          = domain.getOrElse("id", "").toString,
      name        = domain.getOrElse("name", "").toString,
      description = domain.getOrElse("description", "").toString
    )
  }

  def extractEntity(entity: Map[String, Any]): Entity = {
    Entity(
      id          = entity.getOrElse("id", "").toString,
      name        = entity.getOrElse("name", "").toString,
      description = entity.getOrElse("description", "").toString
    )
  }

  // Raw data might contain duplicated context annotations
  def extractEntities(entities: Map[String, Any]): Option[Entities] = {
    if (entities.isEmpty) {
      None
    } else {
      val urls: Option[Seq[Url]] = extractMentionedUrls(entities.getOrElse("urls", List()).asInstanceOf[List[Map[String, Any]]])
      val hashtags: Option[Seq[String]] = extractHashtags(entities.getOrElse("hashtags", List()).asInstanceOf[List[Map[String, Any]]])
      // entities object could contain mentions (users), hashtags, urls, cashtags ans other attributes
      // even if entities object is not empty, urls and hashtags could still be empty
      if (urls.isEmpty && hashtags.isEmpty) {
        None
      } else {
        Some(Entities(mentionedUrls = urls, hashtags = hashtags))
      }
    }
  }

  // extract all users within "includes" data map, contains both author and mentioned users
  def extractUsers(userMap: List[Map[String, Any]]): Option[Seq[User]] = {
    if (userMap.isEmpty) {
      None
    } else {
      Some(
        userMap.distinct.flatMap(user =>
          List(
            User(
              id              = user.getOrElse("id", "").toString,
              name            = user.getOrElse("name", "").toString,
              username        = user.getOrElse("username", "").toString,
              createdAt       = user.getOrElse("created_at", "").toString,
              description     = user.get("description").asInstanceOf[Option[String]],
              location        = user.get("location").asInstanceOf[Option[String]],
              profileImageUrl = user.get("profile_image_url").asInstanceOf[Option[String]],
              metrics         = extractUserMetrics(user.getOrElse("public_metrics", Map()).asInstanceOf[Map[String, Any]]),
              url             = user.get("url").asInstanceOf[Option[String]],
              verified        = user.getOrElse("verified", false).asInstanceOf[Boolean])
            )
          )
        )
      }
  }

  def extractUserMetrics(metrics: Map[String, Any]): Option[UserMetrics] = {
    if (metrics.isEmpty) {
      None
    } else {
      Some(
        UserMetrics(
          followersCount = metrics.getOrElse("followers_count", 0).asInstanceOf[Int],
          followingCount = metrics.getOrElse("following_count", 0).asInstanceOf[Int],
          tweetCount     = metrics.getOrElse("tweet_count", 0).asInstanceOf[Int],
          listedCount    = metrics.getOrElse("listed_count", 0).asInstanceOf[Int]
        )
      )
    }
  }

  // return mentioned users, the author excluded, if presents
  def extractMentionedUsers(author: Option[User]=None, users: Option[Seq[User]]): Option[Seq[User]] = {
    (author, users) match {
      case (Some(author: User), Some(users: Seq[User])) =>
        val mentionedUsers = users.filter(user => !author.id.equals(user.id))
        if (mentionedUsers.isEmpty) {
          None
        } else {
          Some(mentionedUsers)
        }
      case (None, Some(users: Seq[User])) =>
        Some(users)
      case (_, _) => users
    }
  }

  def extractMentionedUrls(urlList: List[Map[String, Any]]): Option[Seq[Url]] = {
    if (urlList.isEmpty) {
      None
    } else {
      Some(
        urlList.distinct.flatMap(url =>
          List(
            Url(
              url = url.getOrElse("url", "").toString,
              expandedUrl = url.getOrElse("expanded_url", "").toString,
              displayUrl = url.getOrElse("display_url", "").toString,
            )
          )
        )
      )
    }
  }

  def extractHashtags(hashtags: List[Map[String,Any]]): Option[Seq[String]] ={
    if (hashtags.isEmpty) {
      None
    } else {
      Some(
        hashtags.distinct.flatMap(tag => {
          val t: String = tag.getOrElse("tag", "").toString; if (t == "") List() else List(t)
        })
      )
    }
  }

  def extractRules(ruleList: List[Map[String, Any]]): Option[Seq[MatchingRule]] = {
    if (ruleList.isEmpty) {
      None
    } else {
      Some(
        ruleList.distinct.flatMap(rule => {
          val id: String = rule.getOrElse("id", "").toString
          val tag: String = rule.getOrElse("tag", "").toString
          if (id != "" && tag != "") List(MatchingRule(id, tag)) else List()
        })
      )
    }
  }

  // TODO Test
  def createTweetFromRow(row: Row): Tweet = {
    Tweet(
      id =                if (row.get(0) != null) row.getString(0) else "NO TWEET ID",
      text =              if (row.get(1) != null) row.getString(1) else "NO TWEET TEXT",
      createdAt =         if (row.get(2) != null) Some(row.getString(2)) else None,
      author =            createUser(Some(row.get(3).asInstanceOf[Row])),
      inReplyToUserId =   if (row.get(4) != null) Some(row.getString(4)) else None,
      publicMetrics =     createPublicMetrics(Some(row.get(5).asInstanceOf[Row])),
      nonPublicMetrics =  createNonPublicMetrics(Some(row.get(6).asInstanceOf[Row])),
      context =           createContext(Some(row.get(7).asInstanceOf[Row])),
      entities =          createEntities(Some(row.get(8).asInstanceOf[Row])),
      mentionedUsers =    createMentionedUsers(if (row.get(9) != null) Some(row.getSeq[Row](9)) else None),
      matchingRules =     createMatchingRules(if (row.get(10) != null) Some(row.getSeq[Row](10)) else None),
      conversationId =    if (row.get(11) != null) Some(row.getString(11)) else None,
      source =            if (row.get(12) != null) Some(row.getString(12)) else None,
      lang =              if (row.get(13) != null) Some(row.getString(13)) else None
    )
  }

  def createUser(row: Option[Row]): Option[User] = {
    row match {
      case Some(value: Row) =>
        Some(User(
          id              = if (value.get(0) != null) value.getString(0) else "NO USER ID",
          name            = if (value.get(1) != null) value.getString(1) else "NO USER NAME",
          username        = if (value.get(2) != null) value.getString(2) else "NO USERNAME",
          createdAt       = if (value.get(3) != null) value.getString(3) else "NO USER CREATED DATE",
          description     = if (value.get(4) != null) Some(value.getString(4)) else None,
          location        = if (value.get(5) != null) Some(value.getString(5)) else None,
          profileImageUrl = if (value.get(6) != null) Some(value.getString(6)) else None,
          metrics         = createUserMetrics(Some(value.get(7).asInstanceOf[Row])),
          url             = if (value.get(8) != null) Some(value.getString(8)) else None,
          verified        = if (value.get(9) != null) value.getBoolean(9) else false
        ))
      case _ => None
    }
  }

  def createPublicMetrics(row: Option[Row]): Option[PublicMetrics] = {
    row match {
      case Some(value: Row) =>
        Some(PublicMetrics(
          retweetCount = if (value.get(0) != null) value.getInt(0) else 0,
          replyCount =   if (value.get(1) != null) value.getInt(1) else 0,
          likeCount =    if (value.get(2) != null) value.getInt(2) else 0,
          quoteCount =   if (value.get(3) != null) value.getInt(3) else 0
        ))
      case _ => None
    }
  }

  def createNonPublicMetrics(row: Option[Row]): Option[NonPublicMetrics] = {
    row match {
      case Some(value: Row) =>
        Some(NonPublicMetrics(
          impressionCount =   if (value.get(0) != null) value.getInt(0) else 0,
          urlLinkClicks =     if (value.get(1) != null) value.getInt(1) else 0,
          userProfileClicks = if (value.get(2) != null) value.getInt(2) else 0
        ))
      case _ => None
    }
  }

  def createContext(rows: Option[Row]): Option[Context] = {
    rows match {
      case Some(value: Row) =>
        val domainList: Option[Seq[Row]] =
          if (value.get(0) != null) Some(value.getSeq[Row](0).distinct) else None
        val entityList: Option[Seq[Row]] =
          if (value.get(1) != null) Some(value.getSeq[Row](1).distinct) else None
        if (domainList.isEmpty && entityList.isEmpty) {
          None
        } else {
          val domains: Option[Seq[Domain]] = domainList match {
            case Some(rows: Seq[Row]) =>
              Some(rows.map(row =>
                Domain(
                  id          = if (row.get(0) != null) row.getString(0) else "NO DOMAIN ID",
                  name        = if (row.get(1) != null) row.getString(1) else "NO DOMAIN NAME",
                  description = if (row.get(2) != null) row.getString(2) else "No DOMAIN DESCRIPTION"
                )
              ).distinct)
            case  _ => None
          }
          val entities: Option[Seq[Entity]] = entityList match {
            case Some(rows: Seq[Row]) =>
              Some(rows.map(row =>
                Entity(
                  id          = if (row.get(0) != null) row.getString(0) else "NO ENTITY ID",
                  name        = if (row.get(1) != null) row.getString(1) else "NO ENTITY NAME",
                  description = if (row.get(2) != null) row.getString(2) else "NO ENTITY DESCRIPTION"
                )
              ).distinct)
            case _ => None
          }
          Some(Context(domain = domains, entity = entities))
        }
      case _ => None
    }
  }

  def createEntities(rows: Option[Row]): Option[Entities] =
  {
    rows match {
      case Some(value: Row) =>
        val hashtags: Option[Seq[String]] = if (value.get(0) != null) Some(value.getSeq[String](0).distinct) else None
        val urlRows: Option[Seq[Row]] = if (value.get(1) != null) Some(value.getSeq[Row](1)) else None
        val urls: Option[Seq[Url]] = urlRows match {
          case Some(rows: Seq[Row]) =>
            Some(rows.map(row =>
              Url(
                url         = if (row.get(0) != null) row.getString(0) else "NO URL",
                expandedUrl = if (row.get(1) != null) row.getString(1) else "NO EXPANDED URL",
                displayUrl  = if (row.get(2) != null) row.getString(2) else "NO DISPLAY URL"
              )
            ).distinct)
          case _ => None
        }
        if (hashtags.isEmpty && urls.isEmpty) {
          None
        } else {
          Some(Entities(hashtags = hashtags, mentionedUrls = urls))
        }
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
          followersCount = if (value.get(0) != null) value.getInt(0) else 0,
          followingCount = if (value.get(1) != null) value.getInt(1) else 0,
          tweetCount     = if (value.get(2) != null) value.getInt(2) else 0,
          listedCount    = if (value.get(3) != null) value.getInt(3) else 0
        ))
      case _ => None
    }
  }

  def createMatchingRules(row: Option[Seq[Row]]): Option[Seq[MatchingRule]] = {
    row match {
      case Some(value: Seq[Row]) =>
        Some(value.map(rule =>
          MatchingRule(
            id =  if (rule.get(0) != null) rule.getString(0) else "",
            tag = if (rule.get(1) != null) rule.getString(1) else ""
          )
        ).distinct)
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
