package utils

import rules.RulePayload

object StringUtils {
  trait Operator

  def Append(data: String, value: String): String = {
    data.concat(value)
  }

  def AppendAt(value: String): String = {
    "@".concat(value)
  }

  def AppendHashtag(value: String): String = {
    "#".concat(value)
  }

  def And(left: String, right: String): String = {
    left.concat(" ").concat(right)
  }

  def Or(left: String, right: String): String = {
    "(".concat(left).concat(" OR ").concat(right).concat(")")
  }

  def Not(value: String): String = {
    "-".concat(value)
  }

  def Group(value: String): String = {
    "(".concat(value).concat(")")
  }

  // TODO: build json payload for filtered stream API
  def BuildJsonPayload(payload: RulePayload): String = {
    val payload =
      """
        |
        |""".stripMargin
    payload
  }
}
