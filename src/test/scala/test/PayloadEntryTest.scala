package test

import org.scalatest.funsuite.AnyFunSuite
import rules.{PayloadEntry, Rule}
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
      hashtags = Some("#twitterdev"))
    println(JSONParser.toJson(rule.toPayload))
  }

  test("keyword with userId") {
    val rule = Rule(keyword = Some("keyword"),
      mentionedUserId = Some("@dailyBerlin"))
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
      mentionedUserId = Some("@dailyBerlin"))
    println(JSONParser.toJson(rule.toPayload))
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
    println(JSONParser.toJson(rule.toPayload))
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
    println(JSONParser.toJson(rule.toPayload))
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
    println(JSONParser.toJson(rule.toPayload))
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
      println(JSONParser.toJson(rule.toPayload))
    }
  }

  test("payload's tag extend 128 characters") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(
        value = " ",
        tag = Some("zQuOndFebzbuBAxHrzsxAEnWtAYqLwlUsnfLXKIIsrDwTrvbYdxlyENUKMRogedUGYTKcuHqSFvDpnryZwQuqdngCZXtYeDXuxiLWwfUXSNzVfkaEmFKcJsNItPKebuRG"))
    }
  }
}
