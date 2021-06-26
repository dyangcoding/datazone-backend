package test

import org.scalatest.funsuite.AnyFunSuite
import rules.Rule
import utils.JSONParser

class RuleTest extends AnyFunSuite {

  test("Rule to basic payload") {
    val rule = Rule(keyword = "life happiness")
    println(JSONParser.toJson(rule.toBasicPayload))
  }
}
