package rules

import utils.StringUtils.{And, Append, AppendAt, Group}

case class Payload(operation: String, entries: Seq[PayloadEntry])

case class PayloadEntry(value: String, tag: Option[String] = None) {
  def flatMap(transformer: rules.PayloadEntry => rules.PayloadEntry): rules.PayloadEntry = {
    transformer(this)
  }

  def appendHashtag(hashtag: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(hashtag, payload.value)))
  }

  def appendUserId(userId: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(AppendAt(userId), payload.value)))
  }

  def appendFromUser(fromUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("from:", fromUser), payload.value)))
  }

  def appendToUser(toUser: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("to:", toUser), payload.value)))
  }

  def appendTag(tagValue: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(payload.value, tag=Option(tagValue)))
  }

  def appendEmoji(emoji: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(emoji, payload.value)))
  }

  def appendUrl(url: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(url, payload.value)))
  }

  def appendPhrase(phrase: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(phrase, payload.value)))
  }

  def appendContext(context: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("context:", context), payload.value)))
  }

  def appendEntity(entity: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("entity:", entity), payload.value)))
  }

  def appendConversationId(conversationId: String): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(And(Append("conversationId:", conversationId), payload.value)))
  }

  def group(): PayloadEntry = {
    this.flatMap(payload => PayloadEntry(Group(payload.value)))
  }
}
