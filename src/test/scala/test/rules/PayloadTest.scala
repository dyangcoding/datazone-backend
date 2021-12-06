package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.PayloadEntry

class PayloadTest extends AnyFunSuite {
  test("throw exception with empty payload") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(value = "")
    }
  }

  test("throw exception when payload's value extend 512 characters") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(
        value = "She wholly fat who window extent either formal. Removing welcomed civility or hastened is. Justice elderly but perhaps expense six her are another passage. Full her ten open fond walk not down. For request general express unknown are. He in just mr door body held john down he. So journey greatly or garrets. Draw door kept do so come on open mean. Estimating stimulated how reasonably precaution diminution she simplicity sir but. Questions am sincerity zealously concluded consisted or no gentleman it.LengthExtended")
    }
  }

  test("throw exception when payload's tag extend 128 characters") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(
        value = " ",
        tag = Some("zQuOndFebzbuBAxHrzsxAEnWtAYqLwlUsnfLXKIIsrDwTrvbYdxlyENUKMRogedUGYTKcuHqSFvDpnryZwQuqdngCZXtYeDXuxiLWwfUXSNzVfkaEmFKcJsNItPKebuRG"))
    }
  }

  test("test applying isRetweet") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyIsRetweet(true))
  }

  test("test applying isVerified") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyIsVerified(false))
  }

  test("test applying isReply") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyIsReply(true))
  }

  test("test applying hasHashtags") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasHashtags(false))
  }

  test("test applying hasLinks") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasLinks(true))
  }

  test("test applying hasMedia") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasMedia(false))
  }

  test("test applying hasImages") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasImages(true))
  }

  test("test applying hasVideos") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasVideos(false))
  }

  test("test applying language") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyLanguage("de"))
  }

  test("test applying sample") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applySample(40))
  }
}