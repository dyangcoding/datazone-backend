package tweets

import org.apache.spark.sql.Row
import reactivemongo.api.bson._
import reactivemongo.api.bson.{BSONReader, BSONWriter, Macros}
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
  def flatMap(transformer: tweets.Tweet => tweets.Tweet): tweets.Tweet = {
    transformer(this)
  }
}

case object Tweet {
  def createTweet(json: String): Option[Tweet] = {
    JSONParser.parseJson(json) match {
      case Some(jsonMap: Map[String, Any]) =>
        val dataMap: Option[Map[String, Any]] = jsonMap.get("data").asInstanceOf[Option[Map[String, Any]]]
        val basicTweet = dataMap match {
          case Some(data: Map[String, Any]) =>
            this.buildBasicTweet(data)
          case _ => None
        }
        this.buildBasicTweet(jsonMap) match {
          case Some(tweet) =>
            val includesMap: Option[Map[String, Any]] = jsonMap.get("includes").asInstanceOf[Option[Map[String, Any]]]
            val ruleMap: Option[Map[String, Any]] = jsonMap.get("matching_rule").asInstanceOf[Option[Map[String, Any]]]
            val withData = dataMap match {
              case Some(data: Map[String, Any]) =>
                tweet
                  .flatMap(tweet => applyMetrics(tweet, data))
                  .flatMap(tweet => applyContext(tweet, data))
                  .flatMap(tweet => applyEntities(tweet, data))
              case _ => tweet
            }
            val withUsers = includesMap match {
              case Some(includesMap: Map[String, Any]) =>
                withData
                  .flatMap(tweet => applyMentionedUsers(tweet, includesMap))
              case _ => withData
            }

            val result = ruleMap match {
              case Some(ruleMap: Map[String, Any]) =>
                withUsers
                .flatMap(tweet => applyMatchingRules(tweet, ruleMap))
              case _ => withUsers
            }

            Some(result)
          case None => None
        }
      case _ => None
    }
  }

  def buildBasicTweet(jsonMap: Map[String, Any]): Option[Tweet] = {
    val dataMap: Option[Map[String, Any]] = jsonMap.get("data").asInstanceOf[Option[Map[String, Any]]]
    dataMap match {
      case Some(data: Map[String, Any]) =>
        createBasicTweet(data)
      case _ => None
    }
  }

  private def createBasicTweet(data: Map[String, Any]): Option[Tweet] = {
    val tweet = (data.get("id"), data.get("text")) match {
      case (Some(id: String), Some(text: String)) =>
        Tweet(id=id, text=text).asInstanceOf[Option[Tweet]]
      case _ => None
    }
    tweet
      .flatMap(tweet => applyCreatedAt(tweet, data))
      .flatMap(tweet => applyReplyToUserId(tweet, data))
      .flatMap(tweet => applyReplyToUserId(tweet, data))
      .flatMap(tweet => applySource(tweet, data))
      .flatMap(tweet => applyLanguage(tweet, data))
  }

  private def applyCreatedAt(tweet: Tweet, data: Map[String, Any]): Option[Tweet] = {
     data.get("created_at") match {
      case Some(createdAt: String) =>
        Tweet(id = tweet.id, text = tweet.text, createdAt = Some(createdAt)).asInstanceOf[Option[Tweet]]
      case _ => tweet.asInstanceOf[Option[Tweet]]
    }
  }

  private def applyReplyToUserId(tweet: Tweet, data: Map[String, Any]): Option[Tweet] = {
    data.get("in_reply_to_user_id") match {
      case Some(replyToUserId: String) =>
        Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = Some(replyToUserId)
        ).asInstanceOf[Option[Tweet]]
      case _ => tweet.asInstanceOf[Option[Tweet]]
    }
  }

  private def applyConversationId(tweet: Tweet, data: Map[String, Any]): Option[Tweet] = {
    data.get("conversation_id") match {
      case Some(conversationId: String) =>
        Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = tweet.inReplyToUserId,
          conversationId = Some(conversationId)
        ).asInstanceOf[Option[Tweet]]
      case _ => tweet.asInstanceOf[Option[Tweet]]
    }
  }

  private def applySource(tweet: Tweet, data: Map[String, Any]): Option[Tweet] = {
    data.get("source") match {
      case Some(source: String) =>
        Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = tweet.inReplyToUserId,
          conversationId = tweet.conversationId,
          source = Some(source)
        ).asInstanceOf[Option[Tweet]]
      case _ => tweet.asInstanceOf[Option[Tweet]]
    }
  }

  private def applyLanguage(tweet: Tweet, data: Map[String, Any]): Option[Tweet] = {
    data.get("lang") match {
      case Some(lang: String) =>
        Tweet(
          id = tweet.id,
          text = tweet.text,
          createdAt = tweet.createdAt,
          inReplyToUserId = tweet.inReplyToUserId,
          conversationId = tweet.conversationId,
          source = tweet.source,
          lang = Some(lang)
        ).asInstanceOf[Option[Tweet]]
      case _ => tweet.asInstanceOf[Option[Tweet]]
    }
  }

  def applyMetrics(tweet: Tweet, data: Map[String, Any]): Tweet = {
    val publicMetrics = data.get("public_metrics").asInstanceOf[Option[Map[String, Any]]]
    val nonPublicMetrics = data.get("non_public_metrics").asInstanceOf[Option[Map[String, Any]]]

    val withPublicMetrics = publicMetrics match {
      case Some(metrics: Map[String, Any]) =>
        tweet.flatMap(tweet => applyPublicMetrics(tweet, metrics))
      case _ => tweet
    }

    nonPublicMetrics match {
      case Some(metrics: Map[String, Any]) =>
        withPublicMetrics.flatMap(tweet => applyNonPublicMetrics(tweet, metrics))
      case _ => withPublicMetrics
    }
  }

  def applyPublicMetrics(tweet: Tweet, metrics: Map[String, Any]): Tweet = {
    Tweet(
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
    )
  }

  def applyNonPublicMetrics(tweet: Tweet, metrics: Map[String, Any]): Tweet = {
    Tweet(
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
    )
  }

  def extractPublicMetrics(metrics: Map[String, Any]): Option[PublicMetrics] = {
    PublicMetrics(
      retweetCount = metrics.getOrElse("retweet_count", 0).asInstanceOf[Int],
      replyCount = metrics.getOrElse("reply_count", 0).asInstanceOf[Int],
      likeCount = metrics.getOrElse("like_count", 0).asInstanceOf[Int],
      quoteCount = metrics.getOrElse("quote_count", 0).asInstanceOf[Int]
    ).asInstanceOf[Option[PublicMetrics]]
  }

  def extractNonPublicMetrics(metrics: Map[String, Any]): Option[NonPublicMetrics] = {
    NonPublicMetrics(
      impressionCount = metrics.getOrElse("impression_count", 0).asInstanceOf[Int],
      urlLinkClicks = metrics.getOrElse("url_link_clicks", 0).asInstanceOf[Int],
      userProfileClicks = metrics.getOrElse("user_profile_clicks", 0).asInstanceOf[Int]
    ).asInstanceOf[Option[NonPublicMetrics]]
  }

  def applyContext(tweet: Tweet, data: Map[String, Any]): Tweet = {
    val context = extractContext(data.get("context_annotations").asInstanceOf[Option[List[Map[String, Any]]]])
    context match {
      case Some(_: Seq[Context]) =>
        Tweet(
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
        )
      case _ => tweet
    }
  }

  def applyEntities(tweet: Tweet, dataMap: Map[String, Any]): Tweet = {
    val entities = extractEntities(dataMap.get("entities").asInstanceOf[Option[Map[String, Any]]])
    entities match {
      case Some(_: Seq[Entities]) =>
        Tweet(
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
        )
      case _ => tweet
    }
  }

  def applyMentionedUsers(tweet: Tweet, includesMap: Map[String, Any]): Tweet = {
    val userMap: Option[List[Map[String, Any]]] = includesMap.get("users").asInstanceOf[Option[List[Map[String, Any]]]]
    userMap match {
      case Some(userMap: List[Map[String, Any]]) =>
        val users = extractUsers(userMap)
        val mentionedUsers = extractMentionedUsers(tweet.author.get.id, users)
        Tweet(
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
        )
      case _ => tweet
    }
  }

  def applyMatchingRules(tweet: Tweet, jsonMap: Map[String, Any]): Tweet = {
    val matchingRulesMap: Option[List[Map[String, Any]]] = jsonMap.get("matching_rules").asInstanceOf[Option[List[Map[String, Any]]]]
    val rules = extractRules(matchingRulesMap)
    rules match {
      case Some(_: Seq[MatchingRule]) =>
        Tweet(
          id=tweet.id,
          text=tweet.text,
          createdAt=tweet.createdAt,
          author=tweet.author,
          inReplyToUserId=tweet.inReplyToUserId,
          publicMetrics=tweet.publicMetrics,
          nonPublicMetrics=tweet.nonPublicMetrics,
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
  def extractContext(context: Option[List[Map[String, Any]]]): Option[Seq[Context]] = {
    context match {
      case Some(context: List[Map[String, Any]]) =>
        context.flatMap(context => {
          val domain = extractDomain(context("domain").asInstanceOf[Map[String, Any]])
          val entity = extractEntity(context("entity").asInstanceOf[Map[String, Any]])
          List(Context(domain = domain, entity = entity))
        }).asInstanceOf[Option[Seq[Context]]]
      case _ => None
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
  def extractEntities(entities: Option[Map[String, Any]]): Option[Seq[Entities]] = {
    val urls = extractMentionedUrls(entities.get("urls").asInstanceOf[Option[List[Map[String, Any]]]])
    val hashtags = extractHashtags(entities.get("hashtags").asInstanceOf[Option[List[Map[String, Any]]]])
    List(Entities(mentionedUrls = urls, hashtags = hashtags)).asInstanceOf[Option[Seq[Entities]]]
  }

  // extract all users within "includes" data map, contains both author and mentioned users
  def extractUsers(userMap: List[Map[String, Any]]): Seq[User] = {
    userMap.flatMap(user => List(
      User(
        id = user.getOrElse("id", "").asInstanceOf[String],
        name = user.getOrElse("name", "").asInstanceOf[String],
        username = user.getOrElse("username", "").asInstanceOf[String],
        createdAt = user.getOrElse("created_at", "").asInstanceOf[String],
        description = user.get("description").asInstanceOf[Option[String]],
        location = user.get("location").asInstanceOf[Option[String]],
        profileImageUrl = user.get("profile_image_url").asInstanceOf[Option[String]],
        metrics = extractUserMetrics(user.get("public_metrics").asInstanceOf[Option[Map[String, Any]]]),
        url = user.get("url").asInstanceOf[Option[String]],
        verified = user.getOrElse("verified", false).asInstanceOf[Boolean])
      )
    )
  }

  def extractUserMetrics(metrics: Option[Map[String, Any]]): Option[UserMetrics] = {
    metrics match {
      case Some(metrics: Map[String, Any]) =>
        UserMetrics(
          followersCount = metrics.getOrElse("followers_count", 0).asInstanceOf[Int],
          followingCount = metrics.getOrElse("following_count", 0).asInstanceOf[Int],
          tweetCount = metrics.getOrElse("tweet_count", 0).asInstanceOf[Int],
          listedCount = metrics.getOrElse("listed_count", 0).asInstanceOf[Int]
        ).asInstanceOf[Option[UserMetrics]]
      case _ => None
    }
  }

  // should also consider that both data and includes map are not presents
  def extractAuthor(jsonMap: Map[String, Any]): User = {
    val dataMap: Map[String, Any] = jsonMap("data").asInstanceOf[Map[String, Any]]
    val includesMap: Map[String, Any] = jsonMap.getOrElse("includes", Map()).asInstanceOf[Map[String, Any]]
    val userMap: List[Map[String, Any]] = includesMap.getOrElse("users", List()).asInstanceOf[List[Map[String, Any]]]
    val authorId = dataMap.getOrElse("author_id", "")
    val users = extractUsers(userMap)
    users.filter(u => authorId.equals(u.id)).head
  }

  def extractMentionedUsers(authorId: String, users: Seq[User]): Option[Seq[User]] = {
    users.filter(u => !authorId.equals(u.id)).asInstanceOf[Option[Seq[User]]]
  }

  def extractMentionedUrls(urlList: Option[List[Map[String, Any]]]): Option[Seq[Url]] = {
    urlList match {
      case Some(urls: List[Map[String, Any]]) =>
        urls.flatMap(url => List(
            Url(
              url = url.getOrElse("url", "").asInstanceOf[String],
              expandedUrl = url.getOrElse("expanded_url", "").asInstanceOf[String],
              displayUrl = url.getOrElse("display_url", "").asInstanceOf[String],
            )
          )
        ).asInstanceOf[Option[Seq[Url]]]
      case _ => None
    }
  }

  def extractHashtags(hashtags: Option[List[Map[String,Any]]]): Option[Seq[String]] ={
    hashtags match {
      case Some(hashtags: List[Map[String, Any]]) =>
        hashtags.flatMap(tag => {
          val t = tag.getOrElse("tag", ""); if (t == "") List() else List(t)
        }).asInstanceOf[Option[Seq[String]]]
      case _ => None
    }
  }

  def extractRules(ruleList: Option[List[Map[String, Any]]]): Option[Seq[MatchingRule]] = {
    ruleList match {
      case Some(rules: List[Map[String, Any]]) =>
        rules.flatMap(rule => {
          val id = rule.getOrElse("id", "").toString
          val tag = rule.getOrElse("tag", "").asInstanceOf[String]
          if (id != "" && tag != "") List(MatchingRule(id, tag)) else List()
        }).asInstanceOf[Option[Seq[MatchingRule]]]
      case _ => None
    }
  }

  def createTweetFromRow(row: Row): Tweet = {
    Tweet(
      row.getString(0),                                                                 // id
      row.getString(1),                                                                 // text
      row.get(2).asInstanceOf[Option[String]],                                          // createdAt
      createUser(row.get(3).asInstanceOf[Option[Row]]),                                 // author
      row.get(4).asInstanceOf[Option[String]],                                          // in reply to user id
      createPublicMetrics(row.get(5).asInstanceOf[Option[Row]]),                        // public metrics
      createNonPublicMetrics(row.get(6).asInstanceOf[Option[Row]]),                     // non public metric
      createContext(row.get(7).asInstanceOf[Option[mutable.WrappedArray[Row]]]),        // context
      createEntities(row.get(8).asInstanceOf[Option[mutable.WrappedArray[Row]]]),       // entities
      createMentionedUsers(row.get(9).asInstanceOf[Option[mutable.WrappedArray[Row]]]), // mentioned Users
      createMatchingRules(row.get(10).asInstanceOf[Option[mutable.WrappedArray[Row]]]), // matching Rules
      row.get(11).asInstanceOf[Option[String]],                                         // conversation Id
      row.get(12).asInstanceOf[Option[String]],                                         // source
      row.get(13).asInstanceOf[Option[String]]                                          // lang
    )
  }

  def createUser(row: Option[Row]): Option[User] = {
    row match {
      case Some(value: Row) =>
        User(
          id = value.getString(0),
          name = value.getString(1),
          username = value.getString(2),
          createdAt = value.getString(3),
          description = value.get(4).asInstanceOf[Option[String]],
          location = value.get(5).asInstanceOf[Option[String]],
          profileImageUrl = value.get(6).asInstanceOf[Option[String]],
          metrics = createUserMetrics(value.get(7).asInstanceOf[Option[Row]]),
          url = value.get(8).asInstanceOf[Option[String]],
          verified =value.getBoolean(9)
        ).asInstanceOf[Option[User]]
      case _ => None
    }
  }

  def createPublicMetrics(row: Option[Row]): Option[PublicMetrics] = {
    row match {
      case Some(value: Row) =>
        PublicMetrics(
          retweetCount = value.getInt(0),
          replyCount = value.getInt(1),
          likeCount = value.getInt(2),
          quoteCount = value.getInt(3)
        ).asInstanceOf[Option[PublicMetrics]]
      case _ => None
    }
  }

  def createNonPublicMetrics(row: Option[Row]): Option[NonPublicMetrics] = {
    row match {
      case Some(value: Row) =>
        NonPublicMetrics(
          impressionCount = value.getInt(0),
          urlLinkClicks = value.getInt(1),
          userProfileClicks = value.getInt(2)
        ).asInstanceOf[Option[NonPublicMetrics]]
      case _ => None
    }
  }

  def createContext(rows: Option[mutable.WrappedArray[Row]]): Option[Seq[Context]] = {
    rows match {
      case Some(values: mutable.WrappedArray[Row]) =>
        values.map(context => {
          val domainRow = context.get(0).asInstanceOf[Row]
          val entityRow = context.get(1).asInstanceOf[Row]
          Context(
            domain = Domain(id = domainRow.getString(0), name = domainRow.getString(1), description = domainRow.getString(2)),
            entity = Entity(id = entityRow.getString(0), name = entityRow.getString(1), description = entityRow.getString(2))
          )
        }).asInstanceOf[Option[Seq[Context]]]
      case _ => None
    }
  }

  def createEntities(rows: Option[mutable.WrappedArray[Row]]): Option[Seq[Entities]] =
  {
    rows match {
      case Some(values: mutable.WrappedArray[Row]) =>
        values.map(entity => {
          val hashtags: Option[Seq[String]] = entity.get(0).asInstanceOf[Option[mutable.WrappedArray[String]]]
          val urls: Option[Seq[Url]] = entity.get(1).asInstanceOf[mutable.WrappedArray[Row]].map(urlRow => {
            Url(url = urlRow.getString(0), expandedUrl = urlRow.getString(1), displayUrl = urlRow.getString(2))
          }).asInstanceOf[Option[Seq[Url]]]
          Entities(hashtags = hashtags, mentionedUrls = urls)
        }).asInstanceOf[Option[Seq[Entities]]]
      case _ => None
    }
  }

  def createMentionedUsers(rows: Option[mutable.WrappedArray[Row]]): Option[Seq[User]] = {
    rows match {
      case Some(values: mutable.WrappedArray[Row]) =>
        values.map(userRow => createUser(Some(userRow))).asInstanceOf[Option[Seq[User]]]
      case _ => None
    }
  }

  def createUserMetrics(row: Option[Row]): Option[UserMetrics] = {
    row match {
      case Some(value: Row) =>
        UserMetrics(
          followersCount = value.getInt(0),
          followingCount = value.getInt(1),
          tweetCount = value.getInt(2),
          listedCount = value.getInt(3)
        ).asInstanceOf[Option[UserMetrics]]
      case _ => None
    }
  }

  def createMatchingRules(row: Option[mutable.WrappedArray[Row]]): Option[Seq[MatchingRule]] = {
    row match {
      case Some(value: mutable.WrappedArray[Row]) =>
        value.map(rule => MatchingRule(id = rule.getString(0), tag = rule.getString(1))).asInstanceOf[Option[Seq[MatchingRule]]]
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
