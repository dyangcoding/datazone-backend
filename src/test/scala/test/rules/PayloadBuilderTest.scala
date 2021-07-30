package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.{PayloadBuilder, PayloadEntry, Rule, RuleOptions}
import utils.JSONParser

class PayloadBuilderTest extends AnyFunSuite{
  test("keyword rule") {
    val rule = Rule(keyword = Some("basic keyword searching"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword with phrase") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a happy dev life"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword with hashtag") {
    val rule = Rule(keyword = Some("keyword"),
      hashtags = Some("#twitterdev"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword with userId") {
    val rule = Rule(keyword = Some("keyword"),
      mentionedUserId = Some("@dailyBerlin"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword with fromUser") {
    val rule = Rule(keyword = Some("keyword"),
      fromUser = Some("dailyBerlin"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword with toUser") {
    val rule = Rule(keyword = Some("keyword"),
      toUser = Some("dailyBerlin"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword, phrase, hashtags") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"))
    print(PayloadBuilder(rule).toPayloadEntry)
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("@dailyBerlin"))
    print(PayloadBuilder(rule).toPayloadEntry)
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
    print(PayloadBuilder(rule).toPayloadEntry)
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
    print(PayloadBuilder(rule).toPayloadEntry)
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
    print(PayloadBuilder(rule).toPayloadEntry)
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
      print(PayloadBuilder(rule).toPayloadEntry)
    }
  }
}
