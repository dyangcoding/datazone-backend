package test.rules

import org.mockito.IdiomaticMockito
import org.scalatest.flatspec.AsyncFlatSpec
import rules.{HttpClient, RulesClient}
import utils.JSONParser

class RulesClientTest extends AsyncFlatSpec with IdiomaticMockito {
  val handlerMock: HttpClient = mock[HttpClient]

  it should "throw exception when getting non validate response" in {
    val response: String =
      """
        |nonSense: ""
        |""".stripMargin
    assertThrows[Exception](RulesClient.getResponseMap(response))
  }

  it should "return a Map when getting validate response" in {
    val response: String =
      """
        |{
        |	"data": [
        |   {
        |      "id": "1420007096209313817",
        |       "value": "(covid 19 delta variation) #covid19 #berlin is:verified has:hashtags lang:en sample:30"
        |   }
        | ]
        |}
        |""".stripMargin
    val responseMap = RulesClient.getResponseMap(response)
    assert(responseMap.contains("data"))
  }

  it should "throw exception when getting a empty data object" in {
    val response: String =
      """
        |{
        |	"data": []
        |}
        |""".stripMargin
    val responseMap: Map[String, Any] = JSONParser.parseJson(response).get
    val result = RulesClient.parseResponse(responseMap)
    result match {
      case Left(error) =>
        assert(error.equals("No data Object exists."))
    }
  }

  it should "throw exception when getting a data object with multiple objects" in {
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
    val responseMap: Map[String, Any] = JSONParser.parseJson(response).get
    val result = RulesClient.parseResponse(responseMap)
    result match {
      case Left(error) =>
        assert(error.equals("Data object contains more then one values."))
    }
  }

  it should "throw exception when getting a data object without rule id" in {
    val response: String =
      """
        |{
        |	"data": [
        | {
        |		"value": "puppy has:images",
        |		"tag": "puppy with images",
        |		"id": ""
        |	  }
        | ]
        |}
        |""".stripMargin
    val responseMap: Map[String, Any] = JSONParser.parseJson(response).get
    assertThrows[Exception](RulesClient.parseData(responseMap))
  }

  it should "return a Rule object when getting correct data object" in {
    val response: String =
      """
        |{
        |	"data": [
        | {
        |		"value": "puppy has:images",
        |		"tag": "puppy with images",
        |		"id": "12345678901234567"
        |	  }
        | ]
        |}
        |""".stripMargin
    val responseMap: Map[String, Any] = JSONParser.parseJson(response).get
    val result = RulesClient.parseResponse(responseMap)
    result match {
      case Right(rule) =>
        assert(rule.twitterGenId.get.equals("12345678901234567"))
        assert(rule.tag.get.equals("puppy with images"))
    }
  }

  it should "return empty error when getting no error object" in {
    val response: String =
      """
        |{
        |	"errors":[]
        |}
        |""".stripMargin
    val responseMap: Map[String, Any] = JSONParser.parseJson(response).get
    val result = RulesClient.parseError(responseMap)
    result match {
      case None => assert(true)
    }
  }

  it should "return error when getting error object" in {
    val response: String =
      """
        |{
        |	"errors":[
        |   {
        |       "errors":[
        |         {"parameters":{}, "message": "Rule does not exist"}
        |       ],
        |       "title": "Invalid Request",
        |       "detail": "One or more parameters to your request was invalid.",
        |       "type": "https://api.twitter.com/2/problems/invalid-request"
        |   }
        |  ]
        |}
        |""".stripMargin
    val responseMap: Map[String, Any] = JSONParser.parseJson(response).get
    val result = RulesClient.parseError(responseMap)
    result match {
      case Some(error) => assert(error.nonEmpty)
    }
  }
}
