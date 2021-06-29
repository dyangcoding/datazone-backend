package rules

import utils.StringUtils.{And, Append, AppendAt, Group}

case class Payload(operation: String, entries: Seq[PayloadEntry])

case class PayloadEntry(value: String, tag: Option[String] = None) {
  def flatMap(transformer: rules.PayloadEntry => rules.PayloadEntry): rules.PayloadEntry = {
    transformer(this)
  }

  def applyHashtag(hashtag: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(hashtag, payload.value)))
  }

  def applyUserId(userId: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(AppendAt(userId), payload.value)))
  }

  def applyFromUser(fromUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
  }

  def applyToUser(toUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
  }

  def applyTag(tagValue: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(payload.value, tag=Option(tagValue)))
  }

  def applyEmoji(emoji: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(emoji, payload.value)))
  }

  def applyUrl(url: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(url, payload.value)))
  }

  def applyPhrase(phrase: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Group(phrase), payload.value)))
  }

  def applyContext(context: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
  }

  def applyEntity(entity: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
  }

  def applyConversationId(conversationId: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("conversation_id:", conversationId), payload.value)))
  }

  def applyRetweetsOfUser(retweetsOfUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(payload.value, Append("retweetOf:", retweetsOfUser))))
  }

  def group(): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(Group(payload.value)))
  }
}
