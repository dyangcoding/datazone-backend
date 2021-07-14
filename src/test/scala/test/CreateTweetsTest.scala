package test

import org.scalatest.funsuite.AnyFunSuite
import tweets.Tweet
import utils.JSONParser

class CreateTweetsTest extends AnyFunSuite{
  val basicDocument: String =
    """
      |{
      |  "data": {
      |    "author_id": "25788488",
      |    "created_at": "2021-07-12T09:49:54.000Z",
      |    "id": "1414522394253877249",
      |    "lang": "en",
      |    "text": "RT @bysau_: tungsten cat's delivery service #ironmouseart @ironmouse https://t.co/FqDONJD4ls"
      |  }
      |}
      |""".stripMargin

  val documentWithContext: String =
    """
      |{
      |  "data": {
      |    "attachments": {},
      |    "author_id": "2399847980",
      |    "context_annotations": [
      |      {
      |        "domain": {
      |          "id": "46",
      |          "name": "Brand Category",
      |          "description": "Categories within Brand Verticals that narrow down the scope of Brands"
      |        },
      |        "entity": {
      |          "id": "781974596752842752",
      |          "name": "Services"
      |        }
      |      },
      |      {
      |        "domain": {
      |          "id": "48",
      |          "name": "Product",
      |          "description": "Products created by Brands.  Examples: Ford Explorer, Apple iPhone."
      |        },
      |        "entity": {
      |          "id": "1412579054855671809",
      |          "name": "Google Innovation"
      |        }
      |      }
      |    ],
      |    "created_at": "2021-07-12T09:50:01.000Z",
      |    "id": "1414522424306065410",
      |    "in_reply_to_user_id": "2399847980",
      |    "lang": "fr",
      |    "text": "@senseikira Faut savoir qu'il racontait souvent des mythos au collège etc. 2 ans après j'avais changé de pseudo et je le voyais moins (lycée différents). Mon pseudo était devenu TeeMate et deviner quoi il avait prit le même pseudo sans me le dire (trahison), j'ai jamais oser lui demander"
      |  }
      |}
      |""".stripMargin

  val documentWithEntities: String =
    """
      |{
      |  "data": {
      |    "attachments": {
      |      "media_keys": [
      |        "3_1414298914648301571"
      |      ]
      |    },
      |    "author_id": "3038006302",
      |    "created_at": "2021-07-12T09:50:11.000Z",
      |    "entities": {
      |      "mentions": [
      |        {
      |          "start": 3,
      |          "end": 13,
      |          "username": "k_tea_cat",
      |          "id": "2536515190"
      |        }
      |      ],
      |      "urls": [
      |        {
      |          "start": 31,
      |          "end": 54,
      |          "url": "https://t.co/cOPazeSGZu",
      |          "expanded_url": "https://twitter.com/k_tea_cat/status/1414298916284092416/photo/1",
      |          "display_url": "pic.twitter.com/cOPazeSGZu"
      |        }
      |      ]
      |    },
      |    "id": "1414522467939401733",
      |    "lang": "en",
      |    "text": "RT @k_tea_cat: Police be like: https://t.co/cOPazeSGZu"
      |  }
      |}
      |""".stripMargin

  val documentWithMatchingRules: String =
    """
      |{
      |  "data": {
      |    "author_id": "1342783974620286976",
      |    "created_at": "2021-07-12T09:50:10.000Z",
      |    "id": "1414522463438860289",
      |    "lang": "en",
      |    "text": "RT @themooninjuly: cat cards https://t.co/nHgLWlbRyT"
      |  },
      |  "matching_rules": [
      |    {
      |      "id": 1414212694220157000,
      |      "tag": "happy cats with media"
      |    },
      |    {
      |      "id": 1414212694220157000,
      |      "tag": "demo test"
      |    }
      |  ]
      |}
      |""".stripMargin

  test("build basic tweet") {
    val jsonMap = JSONParser.parseJson(basicDocument).get
    val basicTweet = Tweet.buildBasicTweet(jsonMap)
    println(basicTweet.get)
  }

  test("build tweet with context") {
    val jsonMap = JSONParser.parseJson(documentWithContext).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val withContext = Tweet.applyContext(tweet.get, dataMap)
    println(withContext)
  }

  test("build tweet with entities") {
    val jsonMap = JSONParser.parseJson(documentWithEntities).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val withEntities = Tweet.applyEntities(tweet.get, dataMap)
    println(withEntities)
  }

  test("build tweet with matching rules") {
    val jsonMap = JSONParser.parseJson(documentWithMatchingRules).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val withMatchingRules = Tweet.applyMatchingRules(tweet.get, jsonMap)
    println(withMatchingRules)
  }
}
