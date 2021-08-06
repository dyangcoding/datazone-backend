package rules

import scalaj.http._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import utils.{FileIO, JSONParser}

import java.io.InvalidObjectException

trait HttpClient {
  type RequestHandler = String => Future[String]
  def handleRequest(payload: String): Future[String]
  def getResponseMap(response: String): Map[String, Any]
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
   * handle sending request, return exception if the request can not be performed due to any unknown reasons
   * @param payload json payload for modifying rules
   * @return
   */
  override def handleRequest(payload: String): Future[String] = {
    (for
      { response <- Future { baseRequest.postData(payload).asString }
    } yield response.body).recover {
      case exception: Exception => throw new Exception(exception.getMessage)
    }
  }

  /**
   * converts response to a Map, throws Exception if response format is not a valid json object
   * @param response response as a String
   * @return
   */
  override def getResponseMap(response: String): Map[String, Any] = {
    val result: Option[Map[String, Any]] = JSONParser.parseJson(response)
    result match {
      case Some(map: Map[String, Any]) => map
      case None => throw new Exception("Invalid Response Format: can not parse Response to JSON.")
    }
  }

  /**
   * add rules for a filtered stream, return a exception
   * if no Rule Object can be returned or error occurs
   * @param addPayload payload for adding rules to a filtered stream
   * @param handler request handler for adding rules, default to the local request handler
   * @return
   */
  def addRules(addPayload: String, handler: RequestHandler = handleRequest): Future[Rule] = {
    handleRequest(addPayload) flatMap { response =>
      val resultMap = getResponseMap(response)
      parseResponse(resultMap) match {
        case Right(rule) => Future { rule }
        case Left(_)     => Future.failed(new Exception(parseError(resultMap).get))
      }
    } recover {
      case exception: Exception => throw new Exception(exception.getMessage)
    }
  }

  /**
   * delete rules for a filtered stream, throw a exception if any error occurs
   * do not depend on the response code returned because even if error occurs, the code would be 200
   * @param deletePayload payload for delete a rule from a filtered stream.
   * @param handler request handler for deleting rules, default to the local request handler
   */
  def deleteRules(deletePayload: String, handler: RequestHandler = handleRequest): Future[Boolean] = {
    handleRequest(deletePayload) flatMap { response =>
      val resultMap = getResponseMap(response)
      parseError(resultMap) match {
        case Some(error: String) => Future.failed(new Exception(error))
        case None => Future { true }
      }
    } recover {
      case exception: Exception => throw new Exception(exception.getMessage)
    }
  }

  /**
   * return a Rule Object or error message
   * @param response response as a Map
   * @return
   */
  def parseResponse(response: Map[String, Any]): Either[String, Rule] = {
    try {
      Right(parseData(response).get)
    } catch {
      case exception: Exception => Left(exception.getMessage)
    }
  }

  /**
   * returns a Rule Object with the Twitter generated Id, in case
   * no data Object could be found or more than one Rule related Object were found
   * or no id could be found, throws a Exception
   * @param result response data returned from Twitter API
   * @return a Rule Object with the Twitter generated Id
   */
  def parseData(result: Map[String, Any]): Option[Rule] = {
    val data = result.getOrElse("data", List()).asInstanceOf[List[Map[String, Any]]]
    if (data.isEmpty) {
      throw new NoSuchElementException("No data Object exists.")
    } else if (data.length > 1) {
      throw new InvalidObjectException("Data object contains more then one values.")
    } else {
      val id = data.head.getOrElse("id", "").asInstanceOf[String]
      val tag = data.head.getOrElse("tag", "").asInstanceOf[String]
      if (id.isEmpty) {
        throw new InvalidObjectException("Data Object contains no Id generated from Twitter.")
      } else {
        Some(Rule(id = Some(id), tag = Some(tag)))
      }
    }
  }

  /**
   * returns a formatted error message containing error title and error details if any error occurs
   * @param errors errors as a Map
   * @return a well formatted error message if any error object presents None otherwise
   */
  def parseError(errors: Map[String, Any]): Option[String] = {
    val result = errors.getOrElse("errors", List()).asInstanceOf[List[Map[String, Any]]]
    if (result.isEmpty) {
      None
    } else {
      val title = result.head.getOrElse("title", "No Error Title could be found!").asInstanceOf[String]
      val detail = result.head.getOrElse("detail", "No Error Detail could be found!").asInstanceOf[String]
      Some(new StringBuilder(title).append(": ").append(detail).toString())
    }
  }
}