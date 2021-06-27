package test

import org.scalatest.funsuite.AnyFunSuite
import rules.Rule
import utils.JSONParser

class RuleTest extends AnyFunSuite{
  val rule: Rule = Rule(keyword = "basic keyword searching")

  test("basic rule payload") {
    print(JSONParser.toJson(rule.toBasicPayload))
  }

  test("append hashtag") {
    val ruleWithHashtag = rule.toBasicPayload.appendHashtag("#tweetfilterStream")
    println(JSONParser.toJson(ruleWithHashtag))
  }

  test("append userId") {
    val ruleWithUserId = rule.toBasicPayload.appendUserId("twitterapi")
    println(JSONParser.toJson(ruleWithUserId))
  }

  test("append from userId") {
    val ruleWithFromUserId = rule.toBasicPayload.appendFromUser("twitterdev")
    println(JSONParser.toJson(ruleWithFromUserId))
  }

  test("append to userId") {
    val ruleWithToUserId = rule.toBasicPayload.appendToUser("dailyNews")
    println(JSONParser.toJson(ruleWithToUserId))
  }

  test("append Tag") {
    val ruleWithToTag = rule.toBasicPayload.appendTag("test filtered streaming rules")
    println(JSONParser.toJson(ruleWithToTag))
  }

  test("combine hashtag and userId") {
    val hashtagWithUserid = rule.toBasicPayload
      .appendHashtag("#happiness")
      .appendUserId("twitterDev")
    println(JSONParser.toJson(hashtagWithUserid))
  }
}
