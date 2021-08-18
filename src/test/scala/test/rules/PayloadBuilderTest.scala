package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.{PayloadBuilder, PayloadEntry, Rule, RuleOptions}
import utils.JSONParser

class PayloadBuilderTest extends AnyFunSuite{
  test("keyword rule") {
    val rule = Rule(keyword = Some("basic keyword searching"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("basic keyword searching"))
  }

  test("keyword with phrase") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a happy dev life"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("how to achieve a happy dev life"))
  }

  test("keyword with hashtag") {
    val rule = Rule(keyword = Some("keyword"),
      hashtags = Some("#twitterdev"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#twitterdev"))
  }

  test("keyword with userId") {
    val rule = Rule(keyword = Some("keyword"),
      mentionedUserId = Some("@dailyBerlin"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("@dailyBerlin"))
  }

  test("keyword with fromUser") {
    val rule = Rule(keyword = Some("keyword"),
      fromUser = Some("dailyBerlin"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("dailyBerlin"))
  }

  test("keyword with toUser") {
    val rule = Rule(keyword = Some("keyword"),
      toUser = Some("dailyBerlin"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("dailyBerlin"))
  }

  test("keyword, phrase, hashtags") {
    val rule = Rule(keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#happiness"))
    assert(payloadEntry.value.contains("how to achieve a better dev life"))
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji") {
    val rule = Rule(
      keyword = Some("keyword"),
      phrase = Some("how to achieve a better dev life"),
      hashtags = Some("#happiness"),
      emoji = Some("dummyEmoji"),
      mentionedUserId = Some("@dailyBerlin"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#happiness"))
    assert(payloadEntry.value.contains("dummyEmoji"))
    assert(payloadEntry.value.contains("how to achieve a better dev life"))
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
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#happiness"))
    assert(payloadEntry.value.contains("dummyEmoji"))
    assert(payloadEntry.value.contains("@dailyBerlin"))
    assert(payloadEntry.value.contains("twitterAPI"))
    assert(payloadEntry.value.contains("how to achieve a better dev life"))
    assert(payloadEntry.tag.isEmpty)
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
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#happiness"))
    assert(payloadEntry.value.contains("dummyEmoji"))
    assert(payloadEntry.value.contains("@dailyBerlin"))
    assert(payloadEntry.value.contains("twitterAPI"))
    assert(payloadEntry.value.contains("how to achieve a better dev life"))
    assert(payloadEntry.value.contains("https://twitterFilteredStreamApi"))
    assert(payloadEntry.value.contains("IntellijLife"))
    assert(payloadEntry.tag.isEmpty)
  }

  test("keyword, phrase, hashtags, mentionedUser, emoji, fromUser, toUser, url, retweetsOfUser with tag") {
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
      tag = Some("dummy tag"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#happiness"))
    assert(payloadEntry.value.contains("dummyEmoji"))
    assert(payloadEntry.value.contains("@dailyBerlin"))
    assert(payloadEntry.value.contains("twitterAPI"))
    assert(payloadEntry.value.contains("how to achieve a better dev life"))
    assert(payloadEntry.value.contains("https://twitterFilteredStreamApi"))
    assert(payloadEntry.value.contains("IntellijLife"))
    assert(payloadEntry.tag.nonEmpty)
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
      conversationId = Some("1334987486343299072"),
      tag = Some("dummy tag"))
    val payloadEntry = PayloadBuilder(rule).toPayloadEntry
    assert(payloadEntry.value.contains("keyword"))
    assert(payloadEntry.value.contains("#happiness"))
    assert(payloadEntry.value.contains("dummyEmoji"))
    assert(payloadEntry.value.contains("@dailyBerlin"))
    assert(payloadEntry.value.contains("twitterAPI"))
    assert(payloadEntry.value.contains("how to achieve a better dev life"))
    assert(payloadEntry.value.contains("https://twitterFilteredStreamApi"))
    assert(payloadEntry.value.contains("IntellijLife"))
    assert(payloadEntry.value.contains("10.799022225751871488"))
    assert(payloadEntry.value.contains("Berlin HTW"))
    assert(payloadEntry.value.contains("1334987486343299072"))
    assert(payloadEntry.tag.nonEmpty)
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
