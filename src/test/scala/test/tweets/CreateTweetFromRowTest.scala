package test.tweets

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql._
import tweets.{Tweet, User}

class CreateTweetFromRowTest extends AnyFunSuite {
  test("create no user when null given") {
    assert(Tweet.createUser(None).isEmpty)
  }

  test("create user") {
    val metricsRow: Row = Row(101, 911, 2012, 2020)
    val row: Row = Row(
      "id",
      "name",
      "userName",
      "createdAt",
      "description",
      "location",
      "profileImageUrl",
      metricsRow,
      "url",
      true
    )
    val user = Tweet.createUser(Some(row))
    assert(user.nonEmpty)
    assert(user.get.id.equals("id"))
    assert(user.get.name.equals("name"))
    assert(user.get.username.equals("userName"))
    assert(user.get.createdAt.equals("createdAt"))
    assert(user.get.description.get.equals("description"))
    assert(user.get.profileImageUrl.get.equals("profileImageUrl"))
    assert(user.get.url.get.equals("url"))
    assert(user.get.metrics.nonEmpty)
    assert(user.get.verified)
  }

  test("create user with default values") {
    val row: Row = Row(null, null, null, null, null, null, null, null, null, null)
    val user = Tweet.createUser(Some(row))
    assert(user.nonEmpty)
    assert(user.get.id.nonEmpty)
    assert(user.get.name.nonEmpty)
    assert(user.get.username.nonEmpty)
    assert(user.get.createdAt.nonEmpty)
    assert(user.get.description.isEmpty)
    assert(user.get.profileImageUrl.isEmpty)
    assert(user.get.url.isEmpty)
    assert(user.get.metrics.isEmpty)
    assert(!user.get.verified)
  }

  test("create no public metrics when null given") {
    assert(Tweet.createPublicMetrics(None).isEmpty)
  }

  test("create public metrics") {
    val row: Row = Row(1, 2, 3, 4)
    val publicMetrics = Tweet.createPublicMetrics(Some(row))
    assert(publicMetrics.nonEmpty)
    assert(publicMetrics.get.retweetCount.equals(1))
    assert(publicMetrics.get.replyCount.equals(2))
    assert(publicMetrics.get.likeCount.equals(3))
    assert(publicMetrics.get.quoteCount.equals(4))
  }

  test("create public metrics with default values") {
    val row: Row = Row(null, null, null, null)
    val publicMetrics = Tweet.createPublicMetrics(Some(row))
    assert(publicMetrics.nonEmpty)
    assert(publicMetrics.get.retweetCount.equals(0))
    assert(publicMetrics.get.replyCount.equals(0))
    assert(publicMetrics.get.likeCount.equals(0))
    assert(publicMetrics.get.quoteCount.equals(0))
  }

  test("create public metrics with negative values") {
    val row: Row = Row(-1, 0, 10, 7)
    assertThrows[IllegalArgumentException](Tweet.createPublicMetrics(Some(row)))
  }

  test("create no non public metrics when null given") {
    val nonPublicMetrics = Tweet.createNonPublicMetrics(None)
    assert(nonPublicMetrics.isEmpty)
  }

  test("create non public metrics") {
    val row: Row = Row(100, 200, 300)
    val nonPublicMetrics = Tweet.createNonPublicMetrics(Some(row))
    assert(nonPublicMetrics.nonEmpty)
    assert(nonPublicMetrics.get.impressionCount.equals(100))
    assert(nonPublicMetrics.get.urlLinkClicks.equals(200))
    assert(nonPublicMetrics.get.userProfileClicks.equals(300))
  }

  test("create non public metrics with default values") {
    val row = Row(null, null, null, null)
    val nonPublicMetrics = Tweet.createNonPublicMetrics(Some(row))
    assert(nonPublicMetrics.nonEmpty)
    assert(nonPublicMetrics.get.impressionCount.equals(0))
    assert(nonPublicMetrics.get.urlLinkClicks.equals(0))
    assert(nonPublicMetrics.get.userProfileClicks.equals(0))
  }

  test("create non public metrics with negative values") {
    val row: Row = Row(10, -7, 3)
    assertThrows[IllegalArgumentException](Tweet.createNonPublicMetrics(Some(row)))
  }

  test("create no context when null given") {
    assert(Tweet.createContext(None).isEmpty)
  }

  test("create no context when both domain and entity are empty") {
    val row: Row = Row(null, null)
    assert(Tweet.createContext(Some(row)).isEmpty)
  }

  test("create context") {
    val row: Row = Row(
      List(Row("domainId", "domainName", "domainDesc")),
      List(Row("entityId", "entityName", "entityDesc"))
    )
    val context = Tweet.createContext(Some(row))
    assert(context.nonEmpty)
  }

  test("create context without duplicate entries") {
    val row: Row = Row(
      List(
        Row("domainId", "domainName", "domainDesc"),
        Row("domainId", "domainName", "domainDesc"),
        Row("domainId", "domainName", "domainDesc")
      ),
      List(
        Row("entityId", "entityName", "entityDesc"),
        Row("entityId", "entityName", "entityDesc")
      )
    )
    val context = Tweet.createContext(Some(row))
    assert(context.nonEmpty)
    assert(context.get.domain.get.size.equals(1))
    assert(context.get.entity.get.size.equals(1))
  }

  test("create no entities when null given") {
    assert(Tweet.createEntities(None).isEmpty)
  }

  test("create no entities when both hashtags and urls are empty") {
    val row: Row = Row(null, null)
    assert(Tweet.createEntities(Some(row)).isEmpty)
  }

  test("create entities") {
    val row: Row = Row(
      List("#hashtag", "#berlinDailyLife", "#TwitterDev"),
      List(Row("www.dummyUrl.com", "www.dummyExpandUrl.de", "www.dummyDisplayUrl.cn"))
    )
    assert(Tweet.createEntities(Some(row)).nonEmpty)
  }

  test("create entities without duplicate entries") {
    val row: Row = Row(
      List("#hashtag", "#berlinDailyLife", "#TwitterDev", "#hashtag", "#berlinDailyLife", "#TwitterDev"),
      List(
        Row("www.dummyUrl.com", "www.dummyExpandUrl.de", "www.dummyDisplayUrl.cn"),
        Row("www.dummyUrl.com", "www.dummyExpandUrl.de", "www.dummyDisplayUrl.cn"))
    )
    val entities = Tweet.createEntities(Some(row))
    assert(entities.nonEmpty)
    assert(entities.get.hashtags.get.size.equals(3))
    assert(entities.get.mentionedUrls.get.size.equals(1))
  }

  test("create no mentioned users when null given") {
    assert(Tweet.createMentionedUsers(None, None).isEmpty)
  }

  test("create mentioned users") {
    val metricsRow: Row = Row(101, 911, 2012, 2020)
    val rows: List[Row] = List(
      Row(
        "id",
        "name",
        "userName",
        "createdAt",
        "description",
        "location",
        "profileImageUrl",
        metricsRow,
        "url",
        true
      )
    )
    val users = Tweet.createMentionedUsers(Some(rows), None)
    assert(users.nonEmpty)
  }

  test("create mentioned users without duplicate") {
    val metricsRow: Row = Row(101, 911, 2012, 2020)
    val rows: List[Row] = List(
      Row(
        "id",
        "name",
        "userName",
        "createdAt",
        "description",
        "location",
        "profileImageUrl",
        metricsRow,
        "url",
        true
      ),
      Row(
        "id",
        "name",
        "userName",
        "createdAt",
        "description",
        "location",
        "profileImageUrl",
        metricsRow,
        "url",
        true
      )
    )
    val users = Tweet.createMentionedUsers(Some(rows), None)
    assert(users.nonEmpty)
    assert(users.get.length.equals(1))
  }

  test("create mentioned users without the author") {
    val metricsRow: Row = Row(101, 911, 2012, 2020)
    val rows: List[Row] = List(
      Row(
        "authorId",
        "name",
        "userName",
        "createdAt",
        "description",
        "location",
        "profileImageUrl",
        metricsRow,
        "url",
        true
      ),
      Row(
        "id",
        "name",
        "userName",
        "createdAt",
        "description",
        "location",
        "profileImageUrl",
        metricsRow,
        "url",
        true
      )
    )
    val author = Some(User(id = "authorId", name = "name", username = "username", createdAt = "createdAt", verified = true))
    val users = Tweet.createMentionedUsers(Some(rows), author)
    assert(users.nonEmpty)
    assert(users.get.length.equals(1))
  }

  test("create no user metrics when null given") {
    assert(Tweet.createUserMetrics(None).isEmpty)
  }

  test("create user metrics") {
    val row: Row = Row(101, 911, 2012, 2020)
    val userMetrics = Tweet.createUserMetrics(Some(row))
    assert(userMetrics.nonEmpty)
    assert(userMetrics.get.followersCount.equals(101))
    assert(userMetrics.get.followingCount.equals(911))
    assert(userMetrics.get.tweetCount.equals(2012))
    assert(userMetrics.get.listedCount.equals(2020))
  }

  test("create user metrics with default values") {
    val row: Row = Row(null, null, null, null)
    val userMetrics = Tweet.createUserMetrics(Some(row))
    assert(userMetrics.nonEmpty)
    assert(userMetrics.get.followersCount.equals(0))
    assert(userMetrics.get.followingCount.equals(0))
    assert(userMetrics.get.tweetCount.equals(0))
    assert(userMetrics.get.listedCount.equals(0))
  }

  test("create user metrics with negative values") {
    val row: Row = Row(-10, 7, 110, 57)
    assertThrows[IllegalArgumentException](Tweet.createUserMetrics(Some(row)))
  }

  test("create no matching rules when null given") {
    assert(Tweet.createMatchingRules(None).isEmpty)
  }

  test("create matching rules") {
    val rows: List[Row] = List(Row(
      "dummyRuleId",
      "dummyRuleTag")
    )
    assert(Tweet.createMatchingRules(Some(rows)).nonEmpty)
  }

  test("create matching rules without duplicate") {
    val rows: List[Row] = List(
      Row("dummyRuleId", "dummyRuleTag"),
      Row("dummyRuleId", "dummyRuleTag"),
      Row("dummyRuleId1", "dummyRuleTag1"),
      Row("dummyRuleId1", "dummyRuleTag1"),
    )
    assert(Tweet.createMatchingRules(Some(rows)).get.length.equals(2))
  }

  test("create basic Tweet") {
    val row: Row = Row(
      "id",
      "text",
      "createdAt",
      null,
      "inReplyToUserId",
      null,
      null,
      null,
      null,
      null,
      null,
      "conversationId",
      "source",
      "lang"
    )
    println(Tweet.createTweetFromRow(row))
  }

  test("create Tweet with context") {
    val context: Row = Row(
      List(Row("domainId", "domainName", "domainDesc")),
      List(Row("entityId", "entityName", "entityDesc"))
    )
    val row: Row = Row(
      "id",
      "text",
      "createdAt",
      null,
      "inReplyToUserId",
      null,
      null,
      context,
      null,
      null,
      null,
      "conversationId",
      "source",
      "lang"
    )
    println(Tweet.createTweetFromRow(row))
  }

  test("create Tweet with entities") {
    val entities: Row = Row(
      List("#hashtag", "#berlinDailyLife", "#TwitterDev"),
      List(Row("www.dummyUrl.com", "www.dummyExpandUrl.de", "www.dummyDisplayUrl.cn"))
    )
    val row: Row = Row(
      "id",
      "text",
      "createdAt",
      null,
      "inReplyToUserId",
      null,
      null,
      null,
      entities,
      null,
      null,
      "conversationId",
      "source",
      "lang"
    )
    println(Tweet.createTweetFromRow(row))
  }

  test("create Tweet with mentioned users") {
    val users: List[Row] = List(
      Row("authorId", "name", "userName", "createdAt", "description", "location", "profileImageUrl", null, "url", true),
      Row("id", "name", "userName", "createdAt", "description", "location", "profileImageUrl", null, "url", true))
    val row: Row = Row(
      "id",
      "text",
      "createdAt",
      null,
      "inReplyToUserId",
      null,
      null,
      null,
      null,
      users,
      null,
      "conversationId",
      "source",
      "lang"
    )
    println(Tweet.createTweetFromRow(row))
  }

  test("create Tweet with matching rules") {
    val rules: List[Row] = List(Row("dummyRuleId", "dummyRuleTag"))
    val row: Row = Row(
      "id",
      "text",
      "createdAt",
      null,
      "inReplyToUserId",
      null,
      null,
      null,
      null,
      null,
      rules,
      "conversationId",
      "source",
      "lang"
    )
    println(Tweet.createTweetFromRow(row))
  }
}
