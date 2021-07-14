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

  val emptyContext: String =
    """
      |{
      |  "data": {
      |    "attachments": {},
      |    "author_id": "2399847980",
      |    "context_annotations": [],
      |    "created_at": "2021-07-12T09:50:01.000Z",
      |    "id": "1414522424306065410",
      |    "in_reply_to_user_id": "2399847980",
      |    "lang": "fr",
      |    "text": "@senseikira Faut savoir qu'il racontait souvent des mythos au collège etc. 2 ans après j'avais changé de pseudo et je le voyais moins (lycée différents). Mon pseudo était devenu TeeMate et deviner quoi il avait prit le même pseudo sans me le dire (trahison), j'ai jamais oser lui demander"
      |  }
      |}
      |""".stripMargin

  val withContext: String =
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

  val emptyEntities: String =
    """
      |{
      |  "data": {
      |    "author_id": "3038006302",
      |    "created_at": "2021-07-12T09:50:11.000Z",
      |    "entities": {},
      |    "id": "1414522467939401733",
      |    "lang": "en",
      |    "text": "RT @k_tea_cat: Police be like: https://t.co/cOPazeSGZu"
      |  }
      |}
      |""".stripMargin

  val entitiesWithUsers: String =
    """
      |{
      |  "data": {
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
      |      ]
      |    },
      |    "id": "1414522467939401733",
      |    "lang": "en",
      |    "text": "RT @k_tea_cat: Police be like: https://t.co/cOPazeSGZu"
      |  }
      |}
      |""".stripMargin

  val entitiesWithUrls: String =
    """
      |{
      |  "data": {
      |    "author_id": "1190100095024406528",
      |    "created_at": "2021-07-12T09:50:05.000Z",
      |    "entities": {
      |      "urls": [
      |        {
      |          "start": 45,
      |          "end": 68,
      |          "url": "https://t.co/bmDyBoyB2i",
      |          "expanded_url": "https://twitter.com/billieloirinha/status/1414522442870136835/photo/1",
      |          "display_url": "pic.twitter.com/bmDyBoyB2i"
      |        }
      |      ]
      |    },
      |    "id": "1414522442870136835",
      |    "lang": "pt",
      |    "text": "ela tá o próprio meme \"peituda metida\" sério https://t.co/bmDyBoyB2i"
      |  }
      |}
      |""".stripMargin

  val entitiesWithHashtags: String =
    """
      |{
      |  "data": {
      |    "author_id": "1717198723",
      |    "created_at": "2021-07-12T09:50:06.000Z",
      |    "entities": {
      |      "hashtags": [
      |        {
      |          "start": 103,
      |          "end": 110,
      |          "tag": "鋼の錬金術師"
      |        },
      |        {
      |          "start": 111,
      |          "end": 115,
      |          "tag": "FMA"
      |        }
      |      ]
      |    },
      |    "id": "1414522447341129730",
      |    "lang": "en",
      |    "text": "RT @kay_tee_kat: Sometimes a perfect trio only needs a cute mechanic, a pipsqueak, and a cat detector\n\n#鋼の錬金術師 #FMA https://t.co/9Qb5ZrJ5mO"
      |  }
      |}
      |""".stripMargin

  val fullEntities: String =
    """
      |{
      |  "data": {
      |    "author_id": "1400267201941172231",
      |    "created_at": "2021-07-12T09:50:09.000Z",
      |    "entities": {
      |      "hashtags": [
      |        {
      |          "start": 117,
      |          "end": 125,
      |          "tag": "Binance"
      |        },
      |        {
      |          "start": 126,
      |          "end": 144,
      |          "tag": "BinanceSmartChain"
      |        },
      |        {
      |          "start": 145,
      |          "end": 156,
      |          "tag": "FairLaunch"
      |        },
      |        {
      |          "start": 157,
      |          "end": 169,
      |          "tag": "PancakeSwap"
      |        }
      |      ],
      |      "mentions": [
      |        {
      |          "start": 0,
      |          "end": 13,
      |          "username": "BinanceChain",
      |          "id": "1052454006537314306"
      |        },
      |        {
      |          "start": 14,
      |          "end": 30,
      |          "username": "yooshi_official",
      |          "id": "1169626188"
      |        }
      |      ],
      |      "urls": [
      |        {
      |          "start": 170,
      |          "end": 193,
      |          "url": "https://t.co/am3mSbEHD9",
      |          "expanded_url": "https://twitter.com/babyakudoge/status/1414522458644885504/photo/1",
      |          "display_url": "pic.twitter.com/am3mSbEHD9"
      |        }
      |      ]
      |    },
      |    "id": "1414522458644885504",
      |    "in_reply_to_user_id": "1052454006537314306",
      |    "lang": "en",
      |    "text": "@BinanceChain @yooshi_official 💘 BabyAkuDoge Fair Launch Soon! 💘\n\nTHE BEST MEME TOKEN!\n\n🌈Earn BUSD Just by Holding!\n\n#Binance #BinanceSmartChain #FairLaunch #PancakeSwap https://t.co/am3mSbEHD9"
      |  }
      |}
      |""".stripMargin

  val emptyMatchingRules: String =
    """
      |{
      |  "data": {
      |    "author_id": "2879755510",
      |    "created_at": "2021-07-12T09:50:06.000Z",
      |    "id": "1414522445814435844",
      |    "in_reply_to_user_id": "766966016937263104",
      |    "lang": "fr",
      |    "text": "@RommTBE @masudo1 @Kwaio @Rouben42060482 @le_Parisien Ah et la voiturr reste dans le parking, donc peu ou pas d'essence utilisé"
      |  },
      |  "matching_rules": []
      |}
      |""".stripMargin
  val withMatchingRules: String =
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

  val semiFullDocument: String =
    """
      |{
      |  "data": {
      |    "attachments": {
      |      "media_keys": [
      |        "3_1414522386758651907",
      |        "3_1414522391737348096",
      |        "3_1414522397009534976"
      |      ]
      |    },
      |    "author_id": "1176621624948404224",
      |    "context_annotations": [
      |      {
      |        "domain": {
      |          "id": "66",
      |          "name": "Interests and Hobbies Category",
      |          "description": "A grouping of interests and hobbies entities, like Novelty Food or Destinations"
      |        },
      |        "entity": {
      |          "id": "847888632711061504",
      |          "name": "Personal finance",
      |          "description": "Personal finance"
      |        }
      |      },
      |      {
      |        "domain": {
      |          "id": "67",
      |          "name": "Interests and Hobbies",
      |          "description": "Interests, opinions, and behaviors of individuals, groups, or cultures; like Speciality Cooking or Theme Parks"
      |        },
      |        "entity": {
      |          "id": "847894852779900928",
      |          "name": "Stocks & indices",
      |          "description": "Stocks"
      |        }
      |      }
      |    ],
      |    "created_at": "2021-07-12T09:50:06.000Z",
      |    "entities": {
      |      "cashtags": [
      |        {
      |          "start": 0,
      |          "end": 4,
      |          "tag": "UNH"
      |        },
      |        {
      |          "start": 5,
      |          "end": 8,
      |          "tag": "GS"
      |        },
      |        {
      |          "start": 9,
      |          "end": 13,
      |          "tag": "CAT"
      |        }
      |      ],
      |      "urls": [
      |        {
      |          "start": 244,
      |          "end": 267,
      |          "url": "https://t.co/KCBI8aqOAK",
      |          "expanded_url": "https://bit.ly/3hWgaZf",
      |          "display_url": "bit.ly/3hWgaZf",
      |          "images": [
      |            {
      |              "url": "https://pbs.twimg.com/news_img/1413025773070524419/8ldb19Me?format=jpg&name=orig",
      |              "width": 1200,
      |              "height": 628
      |            },
      |            {
      |              "url": "https://pbs.twimg.com/news_img/1413025773070524419/8ldb19Me?format=jpg&name=150x150",
      |              "width": 150,
      |              "height": 150
      |            }
      |          ],
      |          "status": 200,
      |          "title": "Request Your Trial",
      |          "description": "Request your trial for 10 euros only for 2 days. If you confirm your trial, your 10 euros will be deducted from your subscription purchase",
      |          "unwound_url": "https://best-trading-indicator.com/pages/trial"
      |        },
      |        {
      |          "start": 268,
      |          "end": 291,
      |          "url": "https://t.co/FksD3xa1lH",
      |          "expanded_url": "https://twitter.com/bti_trading/status/1414522447882342401/photo/1",
      |          "display_url": "pic.twitter.com/FksD3xa1lH"
      |        },
      |        {
      |          "start": 268,
      |          "end": 291,
      |          "url": "https://t.co/FksD3xa1lH",
      |          "expanded_url": "https://twitter.com/bti_trading/status/1414522447882342401/photo/1",
      |          "display_url": "pic.twitter.com/FksD3xa1lH"
      |        },
      |        {
      |          "start": 268,
      |          "end": 291,
      |          "url": "https://t.co/FksD3xa1lH",
      |          "expanded_url": "https://twitter.com/bti_trading/status/1414522447882342401/photo/1",
      |          "display_url": "pic.twitter.com/FksD3xa1lH"
      |        }
      |      ]
      |    },
      |    "id": "1414522447882342401",
      |    "lang": "en",
      |    "public_metrics": {
      |      "retweet_count": 0,
      |      "reply_count": 0,
      |      "like_count": 0,
      |      "quote_count": 0
      |    },
      |    "text": "$UNH $GS $CAT H1\nDo you want to finally make consistent profits with your stocks trading?\n\nOur system will help you find the best trades and avoid common mistakes. ! It is based on an advanced mathematical algorithm with a high accuracy rate.\n https://t.co/KCBI8aqOAK https://t.co/FksD3xa1lH"
      |  },
      |  "includes": {
      |    "media": [
      |      {
      |        "media_key": "3_1414522386758651907",
      |        "type": "photo"
      |      },
      |      {
      |        "media_key": "3_1414522391737348096",
      |        "type": "photo"
      |      },
      |      {
      |        "media_key": "3_1414522397009534976",
      |        "type": "photo"
      |      }
      |    ],
      |    "users": [
      |      {
      |        "id": "1176621624948404224",
      |        "name": "Best Trading Indicator",
      |        "username": "bti_trading"
      |      }
      |    ]
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
    val tweet = Tweet.buildBasicTweet(jsonMap)
    println(tweet.get)
  }

  test("build tweet with empty context") {
    val jsonMap = JSONParser.parseJson(emptyContext).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val empty = Tweet.applyContext(tweet.get, dataMap)
    println(empty)
  }

  test("build tweet with context") {
    val jsonMap = JSONParser.parseJson(withContext).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val context = Tweet.applyContext(tweet.get, dataMap)
    println(context)
  }

  test("build tweet empty entities") {
    val jsonMap = JSONParser.parseJson(emptyEntities).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val empty = Tweet.applyEntities(tweet.get, dataMap)
    println(empty)
  }

  test("build tweet with users within entities") {
    val jsonMap = JSONParser.parseJson(entitiesWithUsers).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val users = Tweet.applyEntities(tweet.get, dataMap)
    println(users)
  }

  test("build tweet with urls within entities") {
    val jsonMap = JSONParser.parseJson(entitiesWithUrls).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val urls = Tweet.applyEntities(tweet.get, dataMap)
    println(urls)
  }

  test("build tweet with hashtags within entities") {
    val jsonMap = JSONParser.parseJson(entitiesWithHashtags).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val hashtags = Tweet.applyEntities(tweet.get, dataMap)
    println(hashtags)
  }

  test("build tweet with full entities") {
    val jsonMap = JSONParser.parseJson(fullEntities).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val dataMap = jsonMap("data").asInstanceOf[Map[String, Any]]
    val full = Tweet.applyEntities(tweet.get, dataMap)
    println(full)
  }

  test("build tweet with empty matching rules") {
    val jsonMap = JSONParser.parseJson(emptyMatchingRules).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val emptyRules = Tweet.applyMatchingRules(tweet.get, jsonMap)
    println(emptyRules)
  }

  test("build tweet with matching rules") {
    val jsonMap = JSONParser.parseJson(withMatchingRules).get
    val tweet = Tweet.buildBasicTweet(jsonMap)
    val matchingRules = Tweet.applyMatchingRules(tweet.get, jsonMap)
    println(matchingRules)
  }

  test("create tweet") {
    val tweet = Tweet.createTweet(semiFullDocument)
    println(tweet)
  }
}
