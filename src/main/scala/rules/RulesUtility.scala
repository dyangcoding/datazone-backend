package rules

import scalaj.http._

/**
 * This Utility provides methods to add, validate, retrieve and delete
 * rules to and from a filtered twitter stream.
 * Learn more about filtered streams @ https://developer.twitter.com/en/docs/twitter-
 * api/tweets/filtered-stream/introduction
 *
 * @param bearerToken Bearer Authentication Token
 **/

class RulesUtility(bearerToken: String) {
  /**
   * URL to the twitter api v2 rules endpoint
   * see https://developer.twitter.com/en/docs/twitter-api/tweets/filtered-stream/api-
   * reference/post-tweets-search-stream-rules
   */
  val TWITTER_RULES_ENDPOINT_URL: String =
    "https://api.twitter.com/2/tweets/search/stream/rules"
  /**
   * This method adds rules for a filtered stream.
   * It sends a POST request with a give JSON payload to the twitter filtered stream rules
   * endpoint.
   *
   * @param rulesJSONPayload payload for adding rules to a filtered stream
   * @param dryRun true to test the syntax of your rules without adding them.
   * @throws IllegalArgumentException if rulesPayload is null
   */
  def addRules(rulesJSONPayload: String, dryRun: Boolean = false): Boolean = {
    if (rulesJSONPayload == null) {
      throw new IllegalArgumentException("Rules Payload can't be null.")
    }
    val request: HttpRequest =
      Http(if (dryRun) TWITTER_RULES_ENDPOINT_URL + "?dry_run=true" else
        TWITTER_RULES_ENDPOINT_URL)
        .postData(rulesJSONPayload)
        .header("content-type", "application/json")
        .header("Authorization", "Bearer " + bearerToken)
    val response = request.asString
    response.code match {
      case 201 =>
        if (dryRun) printResponse("Syntax of your rules is correct. (No rules added!)",
          response)
        else printResponse("Successfully added rules to the filtered stream", response)
        true
      case _ =>
        printResponse("Something went wrong receiving the current rules", response)
        false
    }
  }
  /**
   * This method verify rules for a filtered stream without submitting it.
   *
   * @param rulesPayload payload for adding rules to a filtered stream
   */
  def verifyRules(rulesPayload: String): Boolean = this.addRules(rulesPayload, dryRun = true)
  /**
   * This method gets the rules for a filtered stream
   */
  def retrieveRules(): Boolean = {
    // Create GET request to rules endpoint to receive currently set rules.
    // Use Bearer Auth Token
    val request: HttpRequest =
    Http(TWITTER_RULES_ENDPOINT_URL)
      .header("Authorization", "Bearer " + bearerToken)
    // execute request and parse http body as string
    val response: HttpResponse[String] = request.asString
    response.code match {
      case 200 =>
        printResponse("Currently active rules", response)
        true
      case _ =>
        printResponse("Something went wrong receiving the current rules", response)
        false
    }
  }
  /**
   * This method removes rules for a filtered stream
   *
   * @param deleteJSONPayload payload for delete a rule from a filtered stream.
   * @throws IllegalArgumentException if delete payload is null.
   */
  def removeRules(deleteJSONPayload: String): Boolean = {
    if (deleteJSONPayload == null) {
      throw new IllegalArgumentException("Delete Payload can't be null.")
    }
    val request: HttpRequest =
      Http(TWITTER_RULES_ENDPOINT_URL)
        .postData(deleteJSONPayload)
        .header("content-type", "application/json")
        .header("Authorization", "Bearer " + bearerToken)
    val response = request.asString
    response.code match {
      case 200 =>
        printResponse("Successfully deleted rules from the filtered stream", response)
        true
      case _ =>
        printResponse("Something went wrong deleting the provided rules", response)
        false
    }
  }
  private[this] def printResponse(headline: String, response: HttpResponse[String]): Unit = {
    println(headline)
    println("---------------------")
    println(response.body)
  }}