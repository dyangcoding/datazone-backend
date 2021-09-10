package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.{Rule, RuleOptions}

class RuleTest extends AnyFunSuite {
  test("throw exception when keyword extends 256 characters") {
    assertThrows[IllegalArgumentException] {
      Rule(keyword =
        Some("When warming diced bagels, be sure they are room temperature, Silence yearns when you handle with mineral," +
          " Stars tremble with alarm!, Why does the dosi malfunction?, Our enlightened everything for milk is to " +
          "gain others, Queens resist on turbulence at deep!"))
    }
  }

  test("throw exception when mentionedUserId does not start with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(mentionedUserId = Some("twitterAPI"))
    }
  }

  test("throw exception when hashTags does not start with '#'") {
    assertThrows[IllegalArgumentException] {
      Rule(hashtags = Some("filtered Stream"))
    }
  }

  test("throw exception when Url does not start with 'http://' or 'https://'") {
    assertThrows[IllegalArgumentException] {
      Rule(url = Some("docs.twitter.com"))
    }
  }

  test("throw exception when Url starts with invalid prefix") {
    assertThrows[IllegalArgumentException] {
      Rule(url = Some("http//:docs.twitter.com"))
    }
  }

  test("Url with valid prefix") {
    assert(Rule(url = Some("https://developer.twitter.com")).url.forall(url => url.startsWith("https://")))
  }

  test("throw exception when fromUser starts with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(fromUser = Some("@dailyBerlin"))
    }
  }

  test("throw exception when toUser starts with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(toUser = Some("@happinessForEver"))
    }
  }

  test("throw exception when retweetsOfUser starts with '@'") {
    assertThrows[IllegalArgumentException] {
      Rule(retweetsOfUser = Some("@twitterDev"))
    }
  }

  test("matching tweets with hashtags requires has:hashtags enabled") {
    assertThrows[IllegalArgumentException] {
      Rule(hashtags = Some("#ApacheSpark"), options = Some(RuleOptions.apply(hasHashtags = Some(false))))
    }
  }
}
