package rules

import scalaj.http._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import utils.{FileIO, JSONParser}

trait HttpClient {
  type RequestHandler = String => Future[Map[String, Any]]
  def requestHandler(payload: String): Future[Map[String, Any]]
}

/**
 * This Utility provides methods to add, validate, retrieve and delete
 * rules to and from a filtered twitter stream.
 * Learn more about filtered streams @ https://developer.twitter.com/en/docs/twitter-
 * api/tweets/filtered-stream/introduction
 **/

object RulesClient extends HttpClient {
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
   * encapsulate request handling and JSON Object converting as a basic request handler, return exception if
   * the request can not be performed due to any unknown reasons or have trouble to convert Response Object to JSON
   * @param payload json payload for modifying rules
   * @return
   */
  override def requestHandler(payload: String): Future[Map[String, Any]] = {
    (for {
      response    <- Future { baseRequest.postData(payload).asString }
      responseMap <- getResponseMap(response.body)
    } yield responseMap).recover {
      case exception: Exception => throw new Exception(exception.getMessage)
    }
  }

  /**
   * add rules for a filtered stream, if any of the following occurs, return a exception
   * if no Rule Object can be returned or error occurs
   * @param addPayload payload for adding rules to a filtered stream
   * @param handler request handler for adding rules, default to the local request handler
   * @return
   */
  def addRules(addPayload: String, handler: RequestHandler = requestHandler): Future[Rule] = {
    (for {
      responseMap <- handler(addPayload)
      rule        <- parseData(responseMap)
      _           <- parseError(responseMap)
    } yield rule).recover {
      case exception: Exception => throw new Exception(exception.getMessage)
    }
  }

  /**
   * delete rules for a filtered stream, if any of following occurs, return a exception
   * if any error occur
   * @param deletePayload payload for delete a rule from a filtered stream.
   * @param handler request handler for deleting rules, default to the local request handler
   */
  def deleteRules(deletePayload: String, handler: RequestHandler = requestHandler): Future[Boolean] = {
    (for {
      responseMap <- handler(deletePayload)
      _           <- parseError(responseMap)
    } yield true).recover {
      case exception: Exception => throw new Exception(exception.getMessage)
    }
  }

  /**
   * converts response to a Map, throws Exception if response format is not a valid json object
   * @param response response as a String
   * @return
   */
  def getResponseMap(response: String): Future[Map[String, Any]] = {
    val result: Option[Map[String, Any]] = JSONParser.parseJson(response)
    result match {
      case Some(map: Map[String, Any]) => Future { map }
      case None => Future { throw new Exception("Invalid Response Format: can not parse Response to JSON.") }
    }
  }

  /**
   * returns a Rule Object with the Twitter generated Id, in case
   *  -- no data Object could be found
   *  -- more than one Rule related Object were found
   *  -- no id was returned
   *  returns a specified Exception
   * @param result response data returned from Twitter API
   * @return a Rule Object with the Twitter generated Id
   */
  def parseData(result: Map[String, Any]): Future[Rule] = {
    result.getOrElse("data", None) match {
      case data: List[Map[String, Any]] =>
        if (data.isEmpty) Future.failed(new Exception("The data Object is empty."))
        if (data.size > 1) Future.failed(new Exception("The data object contains more then one values."))
        val twitterGenId = data.head.getOrElse("id", "").asInstanceOf[String]
        val tag = data.head.getOrElse("tag", "").asInstanceOf[String]
        if (twitterGenId.isEmpty) Future.failed(new Exception("The Response data contains no Id generated from Twitter."))
        Future { Rule(twitterGenId = Some(twitterGenId), tag = Some(tag)) }
      case None => Future.failed(throw new Exception("No data Object returned from the Response."))
    }
  }

  /**
   * returns a well formatted error message containing error title and error details if any error occurs
   * @param errorMap response Map
   * @return
   */
  def parseError(errorMap: Map[String, Any]): Future[String] = {
    errorMap.getOrElse("errors", None) match {
      case errors: List[Map[String, Any]] =>
        if (errors.isEmpty) Future {""}
        val title = errors.head.getOrElse("title", "No Error Title could be found!").asInstanceOf[String]
        val details = errors.head.getOrElse("details", List()).asInstanceOf[List[String]]
        Future.failed(
          new Exception(new StringBuilder(title).append(": ").append(details.mkString(" ")).toString())
        )
      case None => Future {""}
    }
  }
}