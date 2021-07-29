package test.rules

import org.mockito.IdiomaticMockito
import org.scalatest.flatspec.AsyncFlatSpec
import rules.{HttpClient, RulesClient}
import utils.JSONParser

import scala.concurrent.Future

class RulesClientTest extends AsyncFlatSpec with IdiomaticMockito {
  val handlerMock: HttpClient = mock[HttpClient]

  it should "throw exception when getting a empty data object back" in {
    val response: String =
      """
        |{
        |	"data": []
        |}
        |""".stripMargin
    val responseMap: Future[Map[String, Any]] = Future { JSONParser.parseJson(response).get }
    handlerMock.requestHandler("any") answers responseMap
    assertThrows[Exception]{
      RulesClient.addRules("dummy payload", handler = handlerMock.requestHandler)
    }
  }

  it should "throw exception when getting a data object with multiple objects back" in {
    val response: String =
      """
        |{
        |	"data": [
        |   {
        |		"value": "cat has:images",
        |		"tag": "cats with images",
        |		"id": "1273026480692322304"
        |	  },
        |   {
        |		"value": "cat",
        |		"tag": "cats without images",
        |		"id": "1273026480692322304"
        |	  }
        | ]
        |}
        |""".stripMargin
    val responseMap: Future[Map[String, Any]] = Future { JSONParser.parseJson(response).get }
    handlerMock.requestHandler("any") answers responseMap
    assertThrows[Exception]{
      RulesClient.addRules("dummy payload", handler = handlerMock.requestHandler)
    }
  }
}
