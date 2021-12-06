package rules

case class PayloadBuilder(rule: Rule) {
  // starting point to build a PayloadEntry, note that PayloadEntry requires a non empty string value
  private def toBasicPayload: PayloadEntry = PayloadEntry(value = " ")

  private def applyKeyword(payload: PayloadEntry): PayloadEntry = {
    rule.keyword match {
      case Some(keyword: String) =>
        payload.applyKeyword(keyword).group()
      case _ => payload
    }
  }

  private def applyEmoji(payload: PayloadEntry): PayloadEntry = {
    rule.emoji match {
      case Some(emoji: String) =>
        payload.applyEmoji(emoji)
      case _ => payload
    }
  }

  private def applyMentionedUserId(payload: PayloadEntry): PayloadEntry = {
    rule.mentionedUserId match {
      case Some(mentionedUserId: String) => payload.applyUser(mentionedUserId)
      case _ => payload
    }
  }

  private def applyPhrase(payload: PayloadEntry): PayloadEntry = {
    rule.phrase match {
      case Some(phrase: String) => payload.applyPhrase(phrase)
      case _ => payload
    }
  }

  private def applyHashTags(payload: PayloadEntry): PayloadEntry = {
    rule.hashtags match {
      case Some(hashtags: String) => payload.applyHashtag(hashtags)
      case _ => payload
    }
  }

  private def applyUrl(payload: PayloadEntry): PayloadEntry = {
    rule.url match {
      case Some(url: String) => payload.applyUrl(url)
      case _ => payload
    }
  }

  private def applyFromUser(payload: PayloadEntry): PayloadEntry = {
    rule.fromUser match {
      case Some(fromUser: String) => payload.applyFromUser(fromUser)
      case _ => payload
    }
  }

  private def applyToUser(payload: PayloadEntry): PayloadEntry = {
    rule.toUser match {
      case Some(toUser: String) => payload.applyToUser(toUser)
      case _ => payload
    }
  }

  private def applyRetweetsOfUser(payload: PayloadEntry): PayloadEntry = {
    rule.retweetsOfUser match {
      case Some(retweetsOfUser: String) => payload.applyRetweetsOfUser(retweetsOfUser)
      case _ => payload
    }
  }

  private def applyContext(payload: PayloadEntry): PayloadEntry = {
    rule.context match {
      case Some(context: String) => payload.applyContext(context)
      case _ => payload
    }
  }

  private def applyEntity(payload: PayloadEntry): PayloadEntry = {
    rule.entity match {
      case Some(entity: String) => payload.applyEntity(entity)
      case _ => payload
    }
  }

  private def applyConversationId(payload: PayloadEntry): PayloadEntry = {
    rule.conversationId match {
      case Some(conversationId: String) => payload.applyConversationId(conversationId)
      case _ => payload
    }
  }

  private def applyTag(payload: PayloadEntry): PayloadEntry = {
    rule.tag match {
      case Some(tag: String) => payload.applyTag(tag)
      case _ => payload
    }
  }

  private def applyIsRetweet(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.isRetweet match {
      case Some(isRetweet: Boolean) => payload.applyIsRetweet(isRetweet)
      case _ => payload
    }
  }

  private def applyIsVerified(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.isVerified match {
      case Some(isVerified: Boolean) => payload.applyIsVerified(isVerified)
      case _ => payload
    }
  }

  private def applyIsReply(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.isReply match {
      case Some(isReply: Boolean) => payload.applyIsReply(isReply)
      case _ => payload
    }
  }

  private def applyHasHashtags(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.hasHashtags match {
      case Some(hasHashtags: Boolean) => payload.applyHasHashtags(hasHashtags)
      case _ => payload
    }
  }

  private def applyHasLinks(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.hasLinks match {
      case Some(hasLinks: Boolean) => payload.applyHasLinks(hasLinks)
      case _ => payload
    }
  }

  private def applyHasMedia(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.hasMedia match {
      case Some(hasHasMedia: Boolean) => payload.applyHasMedia(hasHasMedia)
      case _ => payload
    }
  }

  private def applyHasImages(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.hasImages match {
      case Some(hasImage: Boolean) => payload.applyHasImages(hasImage)
      case _ => payload
    }
  }

  private def applyHasVideos(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.hasVideos match {
      case Some(hasVideos: Boolean) => payload.applyHasVideos(hasVideos)
      case _ => payload
    }
  }

  private def applyLanguage(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.language match {
      case Some(lang: String) => payload.applyLanguage(lang)
      case _ => payload.applyLanguage("en")
    }
  }

  private def applySample(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    options.sample match {
      case Some(simple: Int) => payload.applySample(simple)
      case _ => payload.applySample(30)
    }
  }

  private def applyOptionsInternal(payload: PayloadEntry, options: RuleOptions): PayloadEntry = {
    payload
      .flatMap(payload => applyIsRetweet(payload, options))
      .flatMap(payload => applyIsVerified(payload, options))
      .flatMap(payload => applyIsReply(payload, options))
      .flatMap(payload => applyHasHashtags(payload, options))
      .flatMap(payload => applyHasLinks(payload, options))
      .flatMap(payload => applyHasMedia(payload, options))
      .flatMap(payload => applyHasImages(payload, options))
      .flatMap(payload => applyHasVideos(payload, options))
      .flatMap(payload => applyLanguage(payload, options))
      .flatMap(payload => applySample(payload, options))
  }

  private def applyOptions(payload: PayloadEntry): PayloadEntry = {
    rule.options match {
      case Some(options: RuleOptions) => applyOptionsInternal(payload, options)
      case None => applyOptionsInternal(payload, new RuleOptions)
    }
  }

  private def toPayloadEntryInternal: PayloadEntry = {
    toBasicPayload
      .flatMap(payload => applyKeyword(payload))
      .flatMap(payload => applyEmoji(payload))
      .flatMap(payload => applyMentionedUserId(payload))
      .flatMap(payload => applyPhrase(payload))
      .flatMap(payload => applyHashTags(payload))
      .flatMap(payload => applyUrl(payload))
      .flatMap(payload => applyFromUser(payload))
      .flatMap(payload => applyToUser(payload))
      .flatMap(payload => applyRetweetsOfUser(payload))
      .flatMap(payload => applyContext(payload))
      .flatMap(payload => applyEntity(payload))
      .flatMap(payload => applyConversationId(payload))
      .flatMap(payload => applyTag(payload))
  }

  def toPayloadEntry: PayloadEntry = {
    toPayloadEntryInternal
      .flatMap(payload => applyOptions(payload))
  }
}
