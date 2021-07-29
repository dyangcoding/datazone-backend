package test.rules

import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import rules.RulesClient
import utils.JSONParser

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RulesClientTest extends AnyFunSuite with MockFactory {
  test("send request to add rules") {
    val response: String =
      """
        |{
        |	"data": []
        |}
        |""".stripMargin
    val handlerMock = mockFunction[String, Future[Map[String, Any]]]
    handlerMock.expects("dummy payload").returning(Future { JSONParser.parseJson(response).get })
    val result = RulesClient.addRules("dummy payload", handler = handlerMock)
    println(result.value)
  }
}
