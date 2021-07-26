package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.{Rule, RuleOptions}

class RuleTest extends AnyFunSuite {
  test("keyword extends 256 characters") {
    assertThrows[IllegalArgumentException] {
      Rule(keyword =
        Some("When warming diced bagels, be sure they are room temperature, Silence yearns when you handle with mineral," +
          " Stars tremble with alarm!, Why does the dosi malfunction?, Our enlightened everything for milk is to " +
          "gain others, Queens resist on turbulence at deep!"))
    }
  }

  test("mentionedUserId requires '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(mentionedUserId = Some("twitterAPI"))
    }
  }

  test("hashTags requires '#'") {
    assertThrows[IllegalArgumentException] {
      Rule(hashtags = Some("filtered Stream"))
    }
  }

  test("Url requires 'http://' or 'https://' as prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(url = Some("docs.twitter.com"))
    }
  }

  test("Url with invalid prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(url = Some("http//:docs.twitter.com"))
    }
  }

  test("Url with valid prefix") {
    assert(Rule(url = Some("https://developer.twitter.com")).url.forall(url => url.startsWith("https://")))
  }

  test("fromUser requires no '@' as prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(fromUser = Some("@dailyBerlin"))
    }
  }

  test("toUser requires no '@' as prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(toUser = Some("@happinessForEver"))
    }
  }

  test("retweetsOfUser rquires no '@' as prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(retweetsOfUser = Some("@twitterDev"))
    }
  }

  test("matching tweets with hashtags requires has:hashtags enabled") {
    assertThrows[IllegalArgumentException] {
      val rule = Rule(hashtags = Some("#ApacheSpark"), options = Some(RuleOptions.apply(hasHashtags = Some(false))))
      println(rule.toPayloadEntry)
    }
  }
}
