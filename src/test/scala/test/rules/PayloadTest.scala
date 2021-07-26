package test.rules

import org.scalatest.funsuite.AnyFunSuite
import rules.{AddPayload, DeletePayload, Rule}

class PayloadTest extends AnyFunSuite {
  test("basic Add payload") {
    val entries =  List(Rule(keyword = Some("basic keyword searching")).toPayloadEntry)
    val payload = AddPayload(entries)
    println(payload.toJson)
  }

  test("basic dele payload") {
    val entries =  List("1419679212126998536")
    val payload = DeletePayload(entries)
    println(payload.toJson)
  }
}
