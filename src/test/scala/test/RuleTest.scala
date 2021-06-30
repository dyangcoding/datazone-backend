package test

import org.scalatest.funsuite.AnyFunSuite
import rules.{Rule, RuleOptions}

class RuleTest extends AnyFunSuite {
  test("keyword more than 256 characters") {
    assertThrows[IllegalArgumentException] {
      Rule(keyword =
        Some("When warming diced bagels, be sure they are room temperature, " +
          "Silence yearns when you handle with mineral, Stars tremble with alarm!, " +
          "Why does the dosi malfunction?, Our enlightened everything for milk is to " +
          "gain others theosophically, Private happinesses k."))
    }
  }

  test("mentionedUserId without '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(mentionedUserId = Some("twitterAPI"))
    }
  }

  test("hashTags without '#'") {
    assertThrows[IllegalArgumentException] {
      Rule(hashtags = Some("filtered Stream"))
    }
  }

  test("Url without 'http'") {
    assertThrows[IllegalArgumentException] {
      Rule(url = Some("docs.twitter.com"))
    }
  }

  test("Url without valid 'http' or 'https' prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(url = Some("http//:docs.twitter.com"))
    }
  }

  test("Url with valid 'http'") {
    assert(Rule(url = Some("https://developer.twitter.com")).url.forall(url => url.startsWith("https://")))
  }

  test("fromUser with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(fromUser = Some("@dailyBerlin"))
    }
  }

  test("toUser with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(toUser = Some("@happinessForEver"))
    }
  }

  test("retweetsOfUser with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(retweetsOfUser = Some("@twitterDev"))
    }
  }

  test("match tweets with hashtag with has:hashtags disabled") {
    assertThrows[IllegalArgumentException] {
      val rule = Rule(hashtags = Some("#ApacheSpark"), options = Some(RuleOptions.apply(hasHashtags = Some(false))))
      println(rule.toPayload)
    }
  }
}
