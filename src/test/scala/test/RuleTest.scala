package test

import org.scalatest.funsuite.AnyFunSuite
import rules.{BasicRule, FullRule, Rule}
import utils.JSONParser

class RuleTest extends AnyFunSuite{
  test("keyword rule") {
    val rule = FullRule(keyword = "basic keyword searching")
    print(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with phrase") {
    val rule = FullRule(keyword = "keyword", phrase = Option("how to achieve a happy dev life"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with hashtag") {
    val rule = FullRule(keyword = "keyword", hashtags = Option("twitterdev"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with userId") {
    val rule = FullRule(keyword = "keyword", mentionedUserId = Option("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with fromUser") {
    val rule = FullRule(keyword = "keyword", fromUser = Option("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with toUser") {
    val rule = FullRule(keyword = "keyword", toUser = Option("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags") {
    val rule = FullRule(keyword = "keyword", phrase = Option("how to achieve a better dev life"), hashtags = Option("#happiness"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji") {
    val rule = FullRule(
      keyword = "keyword",
      phrase = Option("how to achieve a better dev life"),
      hashtags = Option("#happiness"),
      emoji = Option("dummyEmoji"),
      mentionedUserId = Option("dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser") {
    val rule = FullRule(
      keyword = "keyword",
      phrase = Option("how to achieve a better dev life"),
      hashtags = Option("#happiness"),
      emoji = Option("dummyEmoji"),
      mentionedUserId = Option("dailyBerlin"),
      fromUser = Option("twitterAPI"),
      toUser = Option("twitterDev"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser, url, retweetsOfUser") {
    val rule = FullRule(
      keyword = "keyword",
      phrase = Option("how to achieve a better dev life"),
      hashtags = Option("#happiness"),
      emoji = Option("dummyEmoji"),
      mentionedUserId = Option("dailyBerlin"),
      fromUser = Option("twitterAPI"),
      toUser = Option("twitterDev"),
      url = Option("http:twitterFilteredStreamApi"),
      retweetsOfUser = Option("IntellijLife"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("All") {
    val rule = FullRule(
      keyword = "keyword",
      phrase = Option("how to achieve a better dev life"),
      hashtags = Option("#happiness"),
      emoji = Option("dummyEmoji"),
      mentionedUserId = Option("dailyBerlin"),
      fromUser = Option("twitterAPI"),
      toUser = Option("twitterDev"),
      url = Option("http:twitterFilteredStreamApi"),
      retweetsOfUser = Option("IntellijLife"),
      context = Option("10.799022225751871488"),
      entity = Option("Berlin HTW"),
      conversationId = Option("1334987486343299072"))
    println(JSONParser.toJson(rule.toPayload))
  }
}
