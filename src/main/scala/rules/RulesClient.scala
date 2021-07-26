package rules

import scalaj.http._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import utils.{FileIO, JSONParser}

/**
 * This Utility provides methods to add, validate, retrieve and delete
 * rules to and from a filtered twitter stream.
 * Learn more about filtered streams @ https://developer.twitter.com/en/docs/twitter-
 * api/tweets/filtered-stream/introduction
 **/

object RulesClient {
  val bearerToken: String = FileIO.getToken
  assert(bearerToken.nonEmpty)

  /**
   * URL to the twitter api v2 rules endpoint
   * see https://developer.twitter.com/en/docs/twitter-api/tweets/filtered-stream/api-
   * reference/post-tweets-search-stream-rules
   */
  val TWITTER_RULES_ENDPOINT_URL: String = "https://api.twitter.com/2/tweets/search/stream/rules"

  /**
   * build a basic request with content type and authorization token
   * @return HttpRequest
   */
  def baseRequest: HttpRequest = {
    Http(TWITTER_RULES_ENDPOINT_URL)
      .header("content-type", "application/json")
      .header("Authorization", "Bearer " + bearerToken)
  }

  /**
   * This method adds rules for a filtered stream.
   * It sends a POST request with a give JSON payload to the twitter filtered stream rules
   * endpoint.
   *
   * @param payload payload for adding rules to a filtered stream
   * @return
   */
  def addRules(payload: String): Future[Rule] = {
    try {
      val response: HttpResponse[String] = baseRequest.postData(payload).asString
      val responseMap: Map[String, Any] = getResponseMap(response.body)
      response.code match {
        case 201 => parseResult(responseMap)
        case _ =>
          val message = parseErrorMessage(responseMap)
          Future { throw new Exception(message) }
      }
    } catch {
      case exception: Exception => Future { throw exception }
    }
  }

  /**
   * returns a Rule Object with the Twitter generated Id for further processing,
   * in case no data Object could be found, or more than one Rule related Object were found,
   * or no id was returned, returns error
   * @param result response data returned from Twitter API
   * @return a Rule Object with the Twitter generated Id
   */
  def parseResult(result: Map[String, Any]): Future[Rule] = {
    result.getOrElse("data", None) match {
      case data: List[Map[String, Any]] =>
        if (data.isEmpty) throw new Exception("The data Object is empty.")
        if (data.size > 1) throw new Exception("The data object contains more then one values.")
        val ruleMap = data.head
        val twitterGenId = ruleMap.getOrElse("id", "").asInstanceOf[String]
        val tag = ruleMap.getOrElse("tag", "").asInstanceOf[String]
        if (twitterGenId.isEmpty) throw new Exception("The Response data contains no Id generated from Twitter.")
        Future { Rule(twitterGenId = Some(twitterGenId), tag = Some(tag)) }
      case None => throw new Exception("No data Object returned from the Response.")
    }
  }

  /**
   * This method removes rules for a filtered stream, Twitter API responses with code 200,
   * even though the Action could not be performed, so we do not rely on the
   * status code returned for this action
   *
   * @param deleteJSONPayload payload for delete a rule from a filtered stream.
   */
  def deleteRules(deleteJSONPayload: String): Future[Boolean] = {
    try {
      val response = baseRequest.postData(deleteJSONPayload).asString
      val responseMap: Map[String, Any] = getResponseMap(response.body)
      responseMap.getOrElse("errors", None) match {
        case None => Future { true }
        case _    =>
          val message = parseErrorMessage(responseMap)
          Future { throw new Exception(message) }
      }
    } catch {
      case exception: Exception => Future { throw exception }
    }
  }

  /**
   * converts response to a Map, throws Exception if response format is not a valid json object
   * @param response response as a String
   * @return
   */
  def getResponseMap(response: String): Map[String, Any] = {
    val result: Option[Map[String, Any]] = JSONParser.parseJson(response)
    result match {
      case Some(map: Map[String, Any]) => map
      case None => throw new Exception("Invalid Response Format: can not parse Response to JSON.")
    }
  }

  /**
   *  returns a well formatted error message containing error title and error details
   *  throws a Exception if
   *   -- error list is empty
   *   -- no error object is presents
   * @param result result as a Map
   * @return
   */
  def parseErrorMessage(result: Map[String, Any]): String = {
    result.getOrElse("errors", None) match {
      case errors: List[Map[String, Any]] =>
        if (errors.isEmpty) throw new Exception("The error Object is empty.")
        val error = errors.head
        error.getOrElse("title", "No Error Title could be found!") + ": " + error.getOrElse("detail", "No Error Details!")
      case None => throw new Exception("No Error Object could be found.")
    }
  }
}