package rules

import utils.StringUtils.{And, Append, AppendAt, Group}

case class Payload(operation: String, entries: Seq[PayloadEntry])

case class PayloadEntry(value: String, tag: Option[String] = None) {
  def flatMap(transformer: rules.PayloadEntry => rules.PayloadEntry): rules.PayloadEntry = {
    transformer(this)
  }

  def applyHashtag(hashtag: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, hashtag)))
  }

  def applyUserId(userId: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, AppendAt(userId))))
  }

  def applyFromUser(fromUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("from:", fromUser))))
  }

  def applyToUser(toUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("to:", toUser))))
  }

  def applyEmoji(emoji: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, emoji)))
  }

  def applyUrl(url: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, url)))
  }

  def applyPhrase(phrase: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Group(phrase))))
  }

  def applyContext(context: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("context:", context))))
  }

  def applyEntity(entity: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("entity:", entity))))
  }

  def applyConversationId(conversationId: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("conversation_id:", conversationId))))
  }

  def applyRetweetsOfUser(retweetsOfUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("retweetOf:", retweetsOfUser))))
  }

  def group(): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(Group(payload.value)))
  }

  def applyTag(tagValue: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(payload.value, tag=Option(tagValue)))
  }
}
