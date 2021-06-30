package test

import org.scalatest.funsuite.AnyFunSuite
import rules.Rule
import utils.JSONParser

class PayloadEntryTest extends AnyFunSuite{
  test("keyword rule") {
    val rule = Rule(keyword = Some("basic keyword searching"))
    print(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with phrase") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a happy dev life"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with hashtag") {
    val rule = Rule(keyword = Some("keyword"),
      hashtags = Some("twitterdev"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with userId") {
    val rule = Rule(keyword = Some("keyword"),
      mentionedUserId = Some("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with fromUser") {
    val rule = Rule(keyword = Some("keyword"),
      fromUser = Some("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with toUser") {
    val rule = Rule(keyword = Some("keyword"),
      toUser = Some("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("dailyBerlin"),
      fromUser = Some("twitterAPI"),
      toUser = Some("twitterDev"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser, url, retweetsOfUser") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("dailyBerlin"),
      fromUser = Some("twitterAPI"),
      toUser = Some("twitterDev"),
      url = Some("http:twitterFilteredStreamApi"),
      retweetsOfUser = Some("IntellijLife"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("All") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("dailyBerlin"),
      fromUser = Some("twitterAPI"),
      toUser = Some("twitterDev"),
      url = Some("http:twitterFilteredStreamApi"),
      retweetsOfUser = Some("IntellijLife"),
      context = Some("10.799022225751871488"),
      entity = Some("Berlin HTW"),
      conversationId = Some("1334987486343299072"))
    println(JSONParser.toJson(rule.toPayload))
  }
}
