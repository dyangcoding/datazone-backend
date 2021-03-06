package rules

import utils.JSONParser
import utils.StringUtils.{And, Append, AppendAt, Group}

/**
 * represents Payload containing single or multiple PayloadEntry for inserting Rules
 * @param entries list of PayloadEntry
 */
case class AddPayload(entries: Seq[PayloadEntry]) {
  // Standard Account Setting only allows 25 concurrent rules
  require(entries.nonEmpty && entries.length <= 25, "Please specify at least one PayloadEntry and maximal 25 PayloadEntry's.")

  def toJson: String = {
    val payloadStr = Map("add" -> entries)
    JSONParser.toJson(payloadStr)
  }
}

/**
 * represents Payload containing single or multiple Rule's Id for deleting Rules
 * @param entries list of PayloadEntry
 */
case class DeletePayload(entries: Seq[String]) {
  // Standard Account Setting only allows 25 concurrent rules
  require(entries.nonEmpty, "Please specify at least one Rule Id.")

  def toJson: String = {
    val payloadStr = Map("delete" -> Map("ids" -> entries))
    JSONParser.toJson(payloadStr)
  }
}

case class PayloadEntry(value: String, tag: Option[String] = None) {
  // payload's value must not be empty and not extend 512 characters
  require(value.nonEmpty && value.length <= 512,
        "payload's value must not be empty and not extend 512 characters.")

  // payload's tag could either be empty or must not be extend 128 characters
  require(tag.isEmpty || (tag.forall(_.nonEmpty) && tag.forall(text => text.length <= 128)),
        "payload's tag could either be empty or must not be extend 128 characters")

  def flatMap(transformer: rules.PayloadEntry => rules.PayloadEntry): rules.PayloadEntry = {
    transformer(this)
  }

  def applyKeyword(keyword: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, keyword)))
  }

  def applyHashtag(hashtag: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, hashtag)))
  }

  def applyUser(userId: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, AppendAt(userId))))
  }

  def applyFromUser(fromUser: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("from:", fromUser))))
  }

  def applyToUser(toUser: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("to:", toUser))))
  }

  def applyRetweetsOfUser(retweetsOfUser: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("retweetOf:", retweetsOfUser))))
  }

  def applyEmoji(emoji: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, emoji)))
  }

  def applyUrl(url: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, url)))
  }

  def applyPhrase(phrase: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Group(phrase))))
  }

  def applyContext(context: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("context:", context))))
  }

  def applyEntity(entity: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("entity:", entity))))
  }

  def applyConversationId(conversationId: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("conversation_id:", conversationId))))
  }

  def applyTag(tag: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(tag=Option(tag)))
  }

  // TODO: if false war given, it does not mean that the negative case should be applied as rule options
  def applyIsRetweet: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "is:retweet")))

  def applyIsVerified: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "is:verified")))

  def applyIsReply: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "is:reply")))

  def applyHasHashtags: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "has:hashtags")))

  def applyHasLinks: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "has:links")))

  def applyHasMedia: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "has:media")))

  def applyHasImages: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "has:images")))

  def applyHasVideos: PayloadEntry =
    this.flatMap(payload => payload.copy(value = And(payload.value, "has:videos")))

  def applyLanguage(lang: String): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("lang:", lang))))
  }

  def applySample(sample: Int): PayloadEntry = {
    this.flatMap(payload => payload.copy(value = And(payload.value, Append("sample:", sample.toString))))
  }

  def group(): PayloadEntry = {
    this.flatMap(payload => payload.copy(Group(payload.value)))
  }
}
