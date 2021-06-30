package utils

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
    left.concat(" ").concat(right).strip()
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

  def langList(): Seq[String] =
    List("am", "ar", "hy", "eu", "bn", "bs", "bg", "my", "hr", "ca", "cs", "da", "nl", "en", "et", "fi", "ka",
      "de", "el", "gu", "ht", "iw", "hi", "hi-Latn", "hu", "is", "in", "it", "ja", "kn", "km", "ko", "lo", "lv",
      "lt", "ml", "dv", "mr", "ne", "no", "or", "pa", "ps", "fa", "pl", "pt", "ro", "ru", "sr", "zh-CN", "sd",
      "si", "sk", "sl", "ckb", "es", "sv", "tl", "ta", "te", "th", "zh-TW", "tr", "uk", "ug", "vi", "cy")
}
