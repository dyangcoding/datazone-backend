package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.{PayloadBuilder, PayloadEntry, Rule, RuleOptions}
import utils.JSONParser

class PayloadBuilderTest extends AnyFunSuite{
  test("keyword rule") {
    val rule = Rule(keyword = Some("basic keyword searching"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword with phrase") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a happy dev life"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword with hashtag") {
    val rule = Rule(keyword = Some("keyword"),
      hashtags = Some("#twitterdev"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword with userId") {
    val rule = Rule(keyword = Some("keyword"),
      mentionedUserId = Some("@dailyBerlin"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword with fromUser") {
    val rule = Rule(keyword = Some("keyword"),
      fromUser = Some("dailyBerlin"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword with toUser") {
    val rule = Rule(keyword = Some("keyword"),
      toUser = Some("dailyBerlin"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword, phrase, hashtags") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("@dailyBerlin"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("@dailyBerlin"),
      fromUser = Some("twitterAPI"),
      toUser = Some("twitterDev"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser, url, retweetsOfUser") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("@dailyBerlin"),
      fromUser = Some("twitterAPI"),
      toUser = Some("twitterDev"),
      url = Some("https://twitterFilteredStreamApi"),
      retweetsOfUser = Some("IntellijLife"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("All") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("@dailyBerlin"),
      fromUser = Some("twitterAPI"),
      toUser = Some("twitterDev"),
      url = Some("https://twitterFilteredStreamApi"),
      retweetsOfUser = Some("IntellijLife"),
      context = Some("10.799022225751871488"),
      entity = Some("Berlin HTW"),
      conversationId = Some("1334987486343299072"))
    println(PayloadBuilder(rule).toPayloadEntry)
    println(JSONParser.toJson(rule))
  }

  test("payload's value extend 512 characters") {
    assertThrows[IllegalArgumentException] {
      val rule = Rule(
        keyword = Some("When warming diced bagels, be sure they are room temperature, " +
          "Silence yearns when you handle with mineral, Stars tremble with alarm!, Why does the dosi malfunction?, " +
          "Our enlightened everything for milk is to gain others, Queens resist on turbulence at deep"),
        phrase =  Some("how to achieve a better dev life"),
        hashtags =Some("#happiness"),
        emoji =   Some("dummyEmoji"),
        mentionedUserId = Some("@dailyBerlin"),
        fromUser =Some("twitterAPI"),
        toUser =  Some("twitterDev"),
        url =     Some("https://twitterFilteredStreamApi"),
        retweetsOfUser = Some("IntellijLife"),
        context = Some("10.799022225751871488"),
        entity =  Some("Berlin HTW"),
        conversationId = Some("1334987486343299072"))
      println(PayloadBuilder(rule).toPayloadEntry)
    }
  }
}
