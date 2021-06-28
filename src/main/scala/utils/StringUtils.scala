package utils

import rules.PayloadEntry

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

  def AppendHasHashtags(value: String): String = {
    "has:hashtags".concat(value)
  }

  def AppendHasLinks(value: String): String = {
    "has:links".concat(value)
  }

  def AppendHasMedia(value: String): String = {
    "has:media".concat(value)
  }

  def AppendHasImages(value: String): String = {
    "has:images".concat(value)
  }

  def AppendHasVideos(value: String): String = {
    "has:videos".concat(value)
  }

  def AppendNotHasHashtags(value: String): String = {
    "-has:hashtags".concat(value)
  }

  def AppendNotHasLinks(value: String): String = {
    "-has:links".concat(value)
  }

  def AppendNotHasMedia(value: String): String = {
    "-has:media".concat(value)
  }

  def AppendNotHasImages(value: String): String = {
    "-has:images".concat(value)
  }

  def AppendNotHasVideos(value: String): String = {
    "-has:videos".concat(value)
  }

  def And(left: String, right: String): String = {
    left.concat(" ").concat(right)
  }

  def Or(left: String, right: String): String = {
    left.concat(" OR ").concat(right)
  }

  def Not(value: String): String = {
    "-".concat(value)
  }

  def Group(value: String): String = {
    "(".concat(value).concat(")")
  }

  // TODO: build json payload for filtered stream API
  def BuildJsonPayload(payload: PayloadEntry): String = {
    val payload =
      """
        |
        |""".stripMargin
    payload
  }
}
