package tweets

import org.apache.spark.sql.Row
import reactivemongo.api.bson._
import reactivemongo.api.bson.{BSONReader, BSONWriter, Macros}
import reactivemongo.api.bson.Macros.Annotations.Reader
import tweets.Context.contextSeqReader
import tweets.Entities.entitiesSeqReader
import utils.JSONParser

import scala.collection.mutable
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
case class User(id:               String,           // The unique identifier of this user
                name:             String,           // The name of the user, as they’ve defined it on their profile
                username:         String,           // The Twitter screen name, handle, or alias that this user identifies themselves with
                createdAt:        String="",        // The UTC datetime that the user account was created on Twitter
                description:      String="",        // The text of this user's profile description (also known as bio), if the user provided one
                location:         String="",        // The location specified in the user's profile, if the user provided one
                profileImageUrl:  String="",        // The URL to the profile image for this user, as shown on the user's profile
                metrics:          UserMetrics=null, // Contains details about activity for this user
                url:              String="",        // The URL specified in the user's profile, if present
                verified:         Boolean           // Indicates if this user is a verified Twitter User
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
case class Entities(hashtags: Seq[String]=List(), mentionedUrls: Seq[Url]=List())

case object Entities {
  implicit val mentionedUrlHandler: BSONDocumentHandler[Url] = Macros.handler[Url]

  val entitiesHandler: BSONHandler[Entities] = Macros.handler[Entities]

  val entitiesSeqReader: BSONReader[Seq[Entities]] = BSONReader.iterable[Entities, Seq](entitiesHandler readTry)
  val entitiesSeqWriter: BSONWriter[Seq[Entities]] = BSONWriter.sequence[Entities](entitiesHandler writeTry)
}

/*

 */
case class TweetMetrics(retweetCount: Int, replyCount: Int, likeCount: Int, quoteCount: Int)

case object TweetMetrics {
  implicit val tweetMetrics: BSONDocumentHandler[TweetMetrics] = Macros.handler[TweetMetrics]
}

/*
 A help class to avoid abusing the class "Rule" which in this case will only contains id and text,
 the other 18 attributes are unset.
 */
case class MatchingRule(id: String, tag: String)

case object MatchingRule {
  implicit val matchingRuleHandler: BSONHandler[MatchingRule] = Macros.handler[MatchingRule]
}

// note: remove conversation ID for now
case class Tweet(
        id:                                     String,                   // The unique identifier of the requested Tweet
        text:                                   String,                   // The actual UTF-8 text of the Tweet
        createdAt:                              String,                   // Creation time of the Tweet
        author:                                 User,                     // The Author who posted this Tweet
        inReplyToUserId:                        String="",                // If the represented Tweet is a reply, this field will contain the original Tweet’s author ID
        @Reader(contextSeqReader) context:      Seq[Context]     =List(), // Contains context annotations for the Tweet, default empty list
        @Reader(entitiesSeqReader) entities:    Seq[Entities]    =List(), // Entities which have been parsed out of the text of the Tweet, default empty list
        mentionedUsers:                         Seq[User]        =List(), // Users this tweet has mentioned in the body text
        matchingRules:                          Seq[MatchingRule]=List(), // annotations about which filtered Rule this tweet was matched with, default empty list
        conversationId:                         String="",                // The Tweet ID of the original Tweet of the conversation (which includes direct replies, replies of replies).
        source:                                 String,                   // The name of the app the user Tweeted from
        lang:                                   String                    // Language of the Tweet, if detected by Twitter. Returned as a BCP47 language tag
                )
{
  def flatMap(transformer: tweets.Tweet => tweets.Tweet): tweets.Tweet = {
    transformer(this)
  }
}

case object Tweet {
  def createTweet(json: String): Option[Tweet] = {
    JSONParser.parseJson(json) match {
      case Some(jsonMap: Map[String, Any]) =>
        this.buildBasicTweet(jsonMap) match {
          case Some(tweet) =>
            val dataMap: Map[String, Any] = jsonMap("data").asInstanceOf[Map[String, Any]]
            val result = tweet
                .flatMap(tweet => applyContext(tweet, dataMap))
                .flatMap(tweet => applyEntities(tweet, dataMap))
                .flatMap(tweet => applyMentionedUsers(tweet, jsonMap))
                .flatMap(tweet => applyMatchingRules(tweet, jsonMap))
            Some(result)
          case None => None
        }
      case _ => None
    }
  }

  def buildBasicTweet(jsonMap: Map[String, Any]): Option[Tweet] = {
    val dataMap: Map[String, Any] = jsonMap("data").asInstanceOf[Map[String, Any]]
    (dataMap.get("id"), dataMap.get("text"), dataMap.get("created_at"), extractAuthor(jsonMap),
      dataMap.get("in_reply_to_user_id"), dataMap.get("conversation_id"), dataMap.get("source"), dataMap.get("lang")) match {
      case (Some(id: String), Some(text: String), Some(createdAt: String), author: User,
          Some(inReplyToUserId: String), Some(conversationId: String), Some(source: String), Some(lang: String)) =>
        Some(
          Tweet(
            id=id,
            text=text,
            createdAt=createdAt,
            author=author,
            inReplyToUserId=inReplyToUserId,
            conversationId=conversationId,
            source=source,
            lang=lang
          )
        )
      case _ => None
    }
  }

  def applyContext(tweet: Tweet, dataMap: Map[String, Any]): Tweet = {
    val context = extractContext(dataMap.getOrElse("context_annotations", List()).asInstanceOf[List[Map[String, Any]]])
    context match {
      case context: Seq[Context] =>
        Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          context=context,
          entities=tweet.entities,
          mentionedUsers=tweet.mentionedUsers,
          matchingRules=tweet.matchingRules,
          conversationId=tweet.conversationId,
          source=tweet.source,
          lang=tweet.lang
        )
      case _ => tweet
    }
  }

  def applyEntities(tweet: Tweet, dataMap: Map[String, Any]): Tweet = {
    val entities = extractEntities(dataMap.getOrElse("entities", Map()).asInstanceOf[Map[String, Any]])
    entities match {
      case entities: Seq[Entities] =>
        Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          context=tweet.context,
          entities=entities,
          mentionedUsers=tweet.mentionedUsers,
          matchingRules=tweet.matchingRules,
          conversationId=tweet.conversationId,
          source=tweet.source,
          lang=tweet.lang
        )
      case _ => tweet
    }
  }

  def applyMentionedUsers(tweet: Tweet, jsonMap: Map[String, Any]): Tweet = {
    val authorId = jsonMap("data").asInstanceOf[Map[String, Any]].getOrElse("author_id", "").asInstanceOf[String]
    val includesMap: Map[String, Any] = jsonMap("includes").asInstanceOf[Map[String, Any]]
    val userMap: List[Map[String, Any]] = includesMap.getOrElse("users", List()).asInstanceOf[List[Map[String, Any]]]
    val users = extractUsers(userMap)
    val mentionedUsers = extractMentionedUsers(authorId, users)
    mentionedUsers match {
      case users: Seq[User] =>
        Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          context=tweet.context,
          entities=tweet.entities,
          mentionedUsers=users,
          matchingRules=tweet.matchingRules,
          conversationId=tweet.conversationId,
          source=tweet.source,
          lang=tweet.lang
        )
      case _ => tweet
    }
  }

  def applyMatchingRules(tweet: Tweet, jsonMap: Map[String, Any]): Tweet = {
    val matchingRulesMap: List[Map[String, Any]] = jsonMap("matching_rules").asInstanceOf[List[Map[String, Any]]]
    extractRules(matchingRulesMap) match {
      case rules: Seq[MatchingRule] =>
        Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          context=tweet.context,
          entities=tweet.entities,
          matchingRules=rules,
          conversationId=tweet.conversationId,
          source=tweet.source,
          lang=tweet.lang)
      case _ => tweet
    }
  }

  // Raw data might contain duplicated context annotations
  def extractContext(context: List[Map[String, Any]]): Seq[Context] = {
    context.flatMap(context => {
      val domain = extractDomain(context("domain").asInstanceOf[Map[String, Any]])
      val entity = extractEntity(context("entity").asInstanceOf[Map[String, Any]])
      List(Context(domain = domain, entity = entity))
    })
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
  def extractEntities(entities: Map[String, Any]): Seq[Entities] = {
    val urls = extractMentionedUrls(entities.getOrElse("urls", List())
      .asInstanceOf[List[Map[String, Any]]])
    val hashtags = extractHashtags(entities.getOrElse("hashtags", List())
      .asInstanceOf[List[Map[String, Any]]])
    List(Entities(mentionedUrls = urls, hashtags = hashtags))
  }

  // extract all users within "includes" data map, contains both author and mentioned users
  def extractUsers(userMap: List[Map[String, Any]]): Seq[User] = {
    userMap.flatMap(user => List(
      User(
        id = user.getOrElse("id", "").asInstanceOf[String],
        name = user.getOrElse("name", "").asInstanceOf[String],
        username = user.getOrElse("username", "").asInstanceOf[String],
        createdAt = user.getOrElse("created_at", "").asInstanceOf[String],
        description = user.getOrElse("description", "").asInstanceOf[String],
        location = user.getOrElse("location", "").asInstanceOf[String],
        profileImageUrl = user.getOrElse("profile_image_url", "").asInstanceOf[String],
        metrics = extractUserMetrics(user.getOrElse("public_metrics", Map()).asInstanceOf[Map[String, Any]]),
        url = user.getOrElse("url", "").asInstanceOf[String],
        verified = user.getOrElse("verified", false).asInstanceOf[Boolean])
      )
    )
  }

  def extractUserMetrics(metrics: Map[String, Any]): UserMetrics = {
    UserMetrics(
      followersCount = metrics.getOrElse("followers_count", 0).asInstanceOf[Int],
      followingCount = metrics.getOrElse("following_count", 0).asInstanceOf[Int],
      tweetCount = metrics.getOrElse("tweet_count", 0).asInstanceOf[Int],
      listedCount = metrics.getOrElse("listed_count", 0).asInstanceOf[Int]
    )
  }

  def extractAuthor(jsonMap: Map[String, Any]): User = {
    val dataMap: Map[String, Any] = jsonMap("data").asInstanceOf[Map[String, Any]]
    val includesMap: Map[String, Any] = jsonMap("includes").asInstanceOf[Map[String, Any]]
    val userMap: List[Map[String, Any]] = includesMap.getOrElse("users", List()).asInstanceOf[List[Map[String, Any]]]
    val authorId = dataMap.getOrElse("author_id", "")
    val users = extractUsers(userMap)
    users.filter(u => authorId.equals(u.id)).head
  }

  def extractMentionedUsers(authorId: String, users: Seq[User]): Seq[User] = {
    users.filter(u => !authorId.equals(u.id))
  }

  def extractMentionedUrls(urlList: List[Map[String, Any]]): Seq[Url] = {
    urlList.flatMap(url => List(
        Url(
          url = url.getOrElse("url", "").asInstanceOf[String],
          expandedUrl = url.getOrElse("expanded_url", "").asInstanceOf[String],
          displayUrl = url.getOrElse("display_url", "").asInstanceOf[String],
        )
      )
    )
  }

  def extractHashtags(tagList:List[Map[String,Any]]):List[String]={
    tagList.flatMap(tag => {
      val t = tag.getOrElse("tag", ""); if (t == "") List() else List(t)
    }).asInstanceOf[List[String]]
  }

  def extractRules(ruleList: List[Map[String, Any]]): Seq[MatchingRule] = {
    ruleList.flatMap(rule => {
      val id = rule.getOrElse("id", "").toString
      val tag = rule.getOrElse("tag", "").asInstanceOf[String]
      if (id != "" && tag != "") List(MatchingRule(id, tag)) else List()
    }).asInstanceOf[Seq[MatchingRule]]
  }

  def createTweetFromRow(row: Row): Tweet = {
    Tweet(
      row.getString(0),                                                         // id
      row.getString(1),                                                         // text
      row.getString(2),                                                         // createdAt
      createUser(row.get(3).asInstanceOf[Row]),                                 // author
      row.getString(4),                                                         // in reply to user id
      createContext(row.get(5).asInstanceOf[mutable.WrappedArray[Row]]),        // context
      createEntities(row.get(6).asInstanceOf[mutable.WrappedArray[Row]]),       // entities
      createMentionedUsers(row.get(7).asInstanceOf[mutable.WrappedArray[Row]]), // mentioned Users
      createMatchingRules(row.get(8).asInstanceOf[mutable.WrappedArray[Row]]),  // matching Rules
      row.getString(9),                                                         // conversation Id
      row.getString(10),                                                        // source
      row.getString(11)                                                         // lang
    )
  }

  def createUser(row: Row): User = {
    User(
      id = row.getString(0),
      name = row.getString(1),
      username = row.getString(2),
      createdAt = row.getString(3),
      description = row.getString(4),
      location = row.getString(5),
      profileImageUrl = row.getString(6),
      metrics = createUserMetrics(row.get(7).asInstanceOf[Row]),
      url = row.getString(8),
      verified = row.getBoolean(9)
    )
  }

  def createContext(rows: mutable.WrappedArray[Row]): Seq[Context] = {
    rows.map(context => {
        val domainRow = context.get(0).asInstanceOf[Row]
        val entityRow = context.get(1).asInstanceOf[Row]
        Context(
          domain = Domain(id = domainRow.getString(0), name = domainRow.getString(1), description = domainRow.getString(2)),
          entity = Entity(id = entityRow.getString(0), name = entityRow.getString(1), description = entityRow.getString(2))
        )
      })
  }

  def createEntities(rows: mutable.WrappedArray[Row]): Seq[Entities] =
  {
    rows.map(entity => {
      val hashtags: List[String] = entity.get(0).asInstanceOf[mutable.WrappedArray[String]].toList
      val urls: List[Url] = entity.get(1).asInstanceOf[mutable.WrappedArray[Row]].map(urlRow => {
        Url(url = urlRow.getString(0), expandedUrl = urlRow.getString(1), displayUrl = urlRow.getString(2))
      }).toList
      Entities(hashtags = hashtags, mentionedUrls = urls)
    })
  }

  def createMentionedUsers(rows: mutable.WrappedArray[Row]): Seq[User] = {
    rows.map(userRow => createUser(userRow))
  }

  def createUserMetrics(row: Row): UserMetrics = {
    UserMetrics(
      followersCount = row.getInt(0),
      followingCount = row.getInt(1),
      tweetCount = row.getInt(2),
      listedCount = row.getInt(3)
    )
  }

  def createMatchingRules(row: mutable.WrappedArray[Row]): Seq[MatchingRule] = {
    row.map(rule => MatchingRule(id = rule.getString(0), tag = rule.getString(1)))
  }

  implicit val context:        BSONDocumentHandler[Context]  = Macros.handler[Context]
  implicit val entity :        BSONDocumentHandler[Entity]   = Macros.handler[Entity]
  implicit val domain:         BSONDocumentHandler[Domain]   = Macros.handler[Domain]
  implicit val mentionedUsers: BSONDocumentHandler[User]     = Macros.handler[User]
  implicit val mentionedUrls:  BSONDocumentHandler[Url]      = Macros.handler[Url]
  implicit val entities:       BSONDocumentHandler[Entities] = Macros.handler[Entities]
  implicit val TweetHandler:   BSONDocumentHandler[Tweet]    = Macros.handler[Tweet]
}
