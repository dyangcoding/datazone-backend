package rules

final case class Rule(
     keyword: String, userID: Long, emoji: String,
     phrase: String, hashtags: String, fromUser: String, toUser: String,
     url: String, retweetsOfUser: String, context: String, entity: String,
     conversationId: Long) {
}
