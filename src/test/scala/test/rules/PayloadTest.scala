package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.PayloadEntry

class PayloadTest extends AnyFunSuite {
  test("empty payload") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(value = "")
    }
  }

  test("payload's value extend 512 characters") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(
        value = "She wholly fat who window extent either formal. Removing welcomed civility or hastened is. Justice elderly but perhaps expense six her are another passage. Full her ten open fond walk not down. For request general express unknown are. He in just mr door body held john down he. So journey greatly or garrets. Draw door kept do so come on open mean. Estimating stimulated how reasonably precaution diminution she simplicity sir but. Questions am sincerity zealously concluded consisted or no gentleman it.LengthExtended")
    }
  }

  test("payload's tag extend 128 characters") {
    assertThrows[IllegalArgumentException] {
      PayloadEntry(
        value = " ",
        tag = Some("zQuOndFebzbuBAxHrzsxAEnWtAYqLwlUsnfLXKIIsrDwTrvbYdxlyENUKMRogedUGYTKcuHqSFvDpnryZwQuqdngCZXtYeDXuxiLWwfUXSNzVfkaEmFKcJsNItPKebuRG"))
    }
  }

  test("apply isRetweet") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyIsRetweet)
  }

  test("apply isVerified") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyIsVerified)
  }

  test("apply isReply") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyIsReply)
  }

  test("apply hasHashtags") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasHashtags)
  }

  test("apply hasLinks") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasLinks)
  }

  test("apply hasMedia") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasMedia)
  }

  test("apply hasImages") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasImages)
  }

  test("apply hasVideos") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyHasVideos)
  }

  test("apply language") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applyLanguage("de"))
  }

  test("apply sample") {
    val payloadEntry = PayloadEntry(" ")
    println(payloadEntry.applySample(40))
  }
}