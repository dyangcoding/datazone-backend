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
  Retrieve UserId and Username from the "mentions" List within the entities Object
 */
case class User(userId:String, username:String)

case object User {
  val userHandler: BSONHandler[User] = Macros.handler[User]

  val userSeqReader: BSONReader[Seq[User]] = BSONReader.iterable[User, Seq](userHandler readTry)
  val userSeqWriter: BSONWriter[Seq[User]] = BSONWriter.sequence[User](userHandler writeTry)
}

case class Url(url: String, expandedUrl: String, displayUrl: String)
case object Url {
  val urlHandler: BSONDocumentHandler[Url] = Macros.handler[Url]

  val urlSeqReader: BSONReader[Seq[Url]] = BSONReader.iterable[Url, Seq](urlHandler readTry)
  val urlSeqWriter: BSONWriter[Seq[Url]] = BSONWriter.sequence[Url](urlHandler writeTry)
}

/*
  Entities are JSON objects that provide additional information about hashtags, urls, user mentions, and cashtags associated with a Tweet
  we utilise hashtags, mentionedUsers, mentionedUrls for now
 */
case class Entities(
                     hashtags: Seq[String]      =List(),
                     mentionedUsers: Seq[User]  =List(),
                     mentionedUrls: Seq[Url]    =List()
                   )

case object Entities {
  implicit val mentionedUserHandler: BSONDocumentHandler[User] = Macros.handler[User]
  implicit val mentionedUrlHandler: BSONDocumentHandler[Url] = Macros.handler[Url]

  val entitiesHandler: BSONHandler[Entities] = Macros.handler[Entities]

  val entitiesSeqReader: BSONReader[Seq[Entities]] = BSONReader.iterable[Entities, Seq](entitiesHandler readTry)
  val entitiesSeqWriter: BSONWriter[Seq[Entities]] = BSONWriter.sequence[Entities](entitiesHandler writeTry)
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
                  authorId:                               String,                   // The unique identifier of the User who posted this Tweet
                  @Reader(contextSeqReader) context:      Seq[Context]     =List(), // Contains context annotations for the Tweet, default empty list
                  @Reader(entitiesSeqReader) entities:    Seq[Entities]    =List(), // Entities which have been parsed out of the text of the Tweet, default empty list
                  matchingRules:                          Seq[MatchingRule]=List(), // annotations about which filtered Rule this tweet was matched with, default empty list
                  //source:                                 String,                 // The name of the app the user Tweeted from
                  lang:                                   String                    // Language of the Tweet, if detected by Twitter. Returned as a BCP47 language tag
                )
{
  def flatMap(transformer: tweets.Tweet => tweets.Tweet): tweets.Tweet = {
    transformer(this)
  }
}

case object Tweet {
  // TODO: only utilise data and matching rule maps for now, more of includesMap later on
  //  val includesMap: Map[String, Any] = jsonMap("includes").asInstanceOf[Map[String, Any]]
  def createTweet(json: String): Option[Tweet] = {
    JSONParser.parseJson(json) match {
      case Some(jsonMap: Map[String, Any]) =>
        this.buildBasicTweet(jsonMap) match {
          case Some(tweet) =>
            val dataMap: Map[String, Any] = jsonMap("data").asInstanceOf[Map[String, Any]]
            val tweetWithContext = tweet.flatMap(tweet => applyContext(tweet, dataMap))
            val tweetWithEntities = tweetWithContext.flatMap(tweet => applyEntities(tweet, dataMap))
            val tweetWithMatchingRules = tweetWithEntities.flatMap(tweet => applyMatchingRules(tweet, jsonMap))
            Some(tweetWithMatchingRules)
          case None => None
        }
      case _ => None
    }
  }

  def buildBasicTweet(jsonMap: Map[String, Any]): Option[Tweet] = {
    val dataMap: Map[String, Any] = jsonMap("data").asInstanceOf[Map[String, Any]]
    (dataMap.get("id"), dataMap.get("text"), dataMap.get("created_at"), dataMap.get("author_id"), dataMap.get("lang")) match {
      case (Some(id: String), Some(text: String), Some(createdAt: String), Some(authorId: String), Some(lang: String)) =>
        Some(Tweet(id=id, text=text, createdAt=createdAt, authorId=authorId, lang=lang))
      case _ => None
    }
  }

  def applyContext(tweet: Tweet, dataMap: Map[String, Any]): Tweet = {
    val context = extractContext(dataMap.getOrElse("context_annotations", List()).asInstanceOf[List[Map[String, Any]]])
    context match {
      case context: Seq[Context] => Tweet(id=tweet.id, text=tweet.text, createdAt=tweet.createdAt,
        authorId=tweet.authorId, context=context, entities=tweet.entities, matchingRules=tweet.matchingRules, lang=tweet.lang)
      case _ => tweet
    }
  }

  def applyEntities(tweet: Tweet, dataMap: Map[String, Any]): Tweet = {
    val entities = extractEntities(dataMap.getOrElse("author_id", "").asInstanceOf[String], dataMap.getOrElse("entities", Map()).asInstanceOf[Map[String, Any]])
    entities match {
      case entities: Seq[Entities] => Tweet(id=tweet.id, text=tweet.text, createdAt=tweet.createdAt,
        authorId=tweet.authorId, context=tweet.context, entities=entities, matchingRules=tweet.matchingRules, lang=tweet.lang)
      case _ => tweet
    }
  }

  def applyMatchingRules(tweet: Tweet, jsonMap: Map[String, Any]): Tweet = {
    val matchingRulesMap: List[Map[String, Any]] = jsonMap("matching_rules").asInstanceOf[List[Map[String, Any]]]
    extractRules(matchingRulesMap) match {
      case rules: Seq[MatchingRule] => Tweet(id=tweet.id, text=tweet.text, createdAt=tweet.createdAt,
        authorId=tweet.authorId, context=tweet.context, entities=tweet.entities, matchingRules=rules, lang=tweet.lang)
      case _ => tweet
    }
  }

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

  def extractEntities(authorId: String, entities: Map[String, Any]): Seq[Entities] = {
    val users = extractMentionedUsers(authorId = authorId, userList = entities.getOrElse("mentions", List())
      .asInstanceOf[List[Map[String, Any]]])
    val urls = extractMentionedUrls(entities.getOrElse("urls", List())
      .asInstanceOf[List[Map[String, Any]]])
    val hashtags = extractHashtags(entities.getOrElse("hashtags", List())
      .asInstanceOf[List[Map[String, Any]]])
    List(Entities(mentionedUsers = users, mentionedUrls = urls, hashtags = hashtags))
  }

  def extractMentionedUsers(authorId: String, userList: List[Map[String, Any]]): Seq[User] = {
    val users = userList.flatMap(user => List(
        User(
          userId = user.getOrElse("id", "").asInstanceOf[String],
          username = user.getOrElse("username", "").asInstanceOf[String])
        )
    )
    users.filter(u => !authorId.equals(u.userId))
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
    Tweet(row.getString(0),   // id
          row.getString(1),   // text
          row.getString(2),   // createdAt
          row.getString(3),   // authorID
          createContext(row.get(4).asInstanceOf[mutable.WrappedArray[Row]]),  // context
          createEntities(row.get(5).asInstanceOf[mutable.WrappedArray[Row]]), //entities
          createMatchingRules(row.get(6).asInstanceOf[mutable.WrappedArray[Row]]), // matching Rules
          row.getString(7)    // lang
    )
  }

  def createContext(row: mutable.WrappedArray[Row]): Seq[Context] = {
    row.map(context => {
        val domainRow = context.get(0).asInstanceOf[Row]
        val entityRow = context.get(1).asInstanceOf[Row]
        Context(domain = Domain(id = domainRow.getString(0), name = domainRow.getString(1), description = domainRow.getString(2)),
                entity = Entity(id = entityRow.getString(0), name = entityRow.getString(1), description = entityRow.getString(2))
        )
      })
  }

  def createEntities(row: mutable.WrappedArray[Row]): Seq[Entities] =
  {
    row.map(entities => {
      val hashtags: List[String] = entities.get(0).asInstanceOf[mutable.WrappedArray[String]].toList
      val users: List[User] = entities.get(1).asInstanceOf[mutable.WrappedArray[Row]].map(userRow => {
        User(userId = userRow.getString(0), username = userRow.getString(1))
      }).toList
      val urls: List[Url] = entities.get(2).asInstanceOf[mutable.WrappedArray[Row]].map(urlRow => {
        Url(url = urlRow.getString(0), expandedUrl = urlRow.getString(1), displayUrl = urlRow.getString(2))
      }).toList
      Entities(hashtags = hashtags, mentionedUsers = users, mentionedUrls = urls)
    })
  }

  def createMatchingRules(row: mutable.WrappedArray[Row]): Seq[MatchingRule] = {
    row.map(ruleRow => MatchingRule(id = ruleRow.getString(0), tag = ruleRow.getString(1)))
  }

  implicit val context:        BSONDocumentHandler[Context]  = Macros.handler[Context]
  implicit val entity :        BSONDocumentHandler[Entity]   = Macros.handler[Entity]
  implicit val domain:         BSONDocumentHandler[Domain]   = Macros.handler[Domain]
  implicit val mentionedUsers: BSONDocumentHandler[User]     = Macros.handler[User]
  implicit val mentionedUrls:  BSONDocumentHandler[Url]      = Macros.handler[Url]
  implicit val entities:       BSONDocumentHandler[Entities] = Macros.handler[Entities]
  implicit val TweetHandler:   BSONDocumentHandler[Tweet]    = Macros.handler[Tweet]
}
