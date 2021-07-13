package test

import org.scalatest.funsuite.AnyFunSuite
import tweets.Tweet

import scala.util.{Failure, Success}

class CreateTweetsTest extends AnyFunSuite{
  val jsonDocument =
    """
      |{
      |  "data": {
      |    "attachments": {},
      |    "author_id": "1393894475915694084",
      |    "created_at": "2021-07-12T09:49:49.000Z",
      |    "entities": {
      |      "annotations": [
      |        {
      |          "start": 51,
      |          "end": 58,
      |          "probability": 0.4898,
      |          "type": "Person",
      |          "normalized_text": "Camellia"
      |        },
      |        {
      |          "start": 64,
      |          "end": 71,
      |          "probability": 0.6153,
      |          "type": "Person",
      |          "normalized_text": "Camellia"
      |        }
      |      ],
      |      "mentions": [
      |        {
      |          "start": 3,
      |          "end": 11,
      |          "username": "cametek",
      |          "id": "70876713"
      |        }
      |      ]
      |    },
      |    "id": "1414522377325608960",
      |    "lang": "en",
      |    "public_metrics": {
      |      "retweet_count": 1,
      |      "reply_count": 0,
      |      "like_count": 0,
      |      "quote_count": 0
      |    },
      |    "referenced_tweets": [
      |      {
      |        "type": "retweeted",
      |        "id": "1414522348581965827"
      |      }
      |    ],
      |    "text": "RT @cametek: yes, that's why I credit the song as \"Camellia ft. Camellia\" (sometimes not though). that's originally a kinda meme, but it tu…"
      |  },
      |  "includes": {
      |    "users": [
      |      {
      |        "id": "1393894475915694084",
      |        "name": "Tokitsune.kanata😈💫🔮🐉🏴‍☠️🐻",
      |        "username": "TokitsuneK"
      |      },
      |      {
      |        "id": "70876713",
      |        "name": "かめりあ/Camellia 𝙉𝙀𝙒𝘼𝙇𝘽𝙐𝙈 𝕌.𝕌.𝔽.𝕆. July11",
      |        "username": "cametek"
      |      }
      |    ],
      |    "tweets": [
      |      {
      |        "attachments": {},
      |        "author_id": "70876713",
      |        "created_at": "2021-07-12T09:49:43.000Z",
      |        "entities": {
      |          "annotations": [
      |            {
      |              "start": 38,
      |              "end": 45,
      |              "probability": 0.5214,
      |              "type": "Person",
      |              "normalized_text": "Camellia"
      |            },
      |            {
      |              "start": 51,
      |              "end": 58,
      |              "probability": 0.6374,
      |              "type": "Person",
      |              "normalized_text": "Camellia"
      |            }
      |          ],
      |          "urls": [
      |            {
      |              "start": 216,
      |              "end": 239,
      |              "url": "https://t.co/399o5231Cl",
      |              "expanded_url": "https://twitter.com/itsmagma_/status/1414514921488928768",
      |              "display_url": "twitter.com/itsmagma_/stat…"
      |            }
      |          ]
      |        },
      |        "id": "1414522348581965827",
      |        "lang": "en",
      |        "public_metrics": {
      |          "retweet_count": 1,
      |          "reply_count": 0,
      |          "like_count": 3,
      |          "quote_count": 0
      |        },
      |        "referenced_tweets": [
      |          {
      |            "type": "quoted",
      |            "id": "1414514921488928768"
      |          }
      |        ],
      |        "text": "yes, that's why I credit the song as \"Camellia ft. Camellia\" (sometimes not though). that's originally a kinda meme, but it turns out great way to explain it in short that \"I composed it, wrote lyrics and I sang it\" https://t.co/399o5231Cl"
      |      }
      |    ]
      |  },
      |  "matching_rules": [
      |    {
      |      "id": 1414212694220157000,
      |      "tag": "funny things"
      |    }
      |  ]
      |}
      |""".stripMargin

  test("getDataAsTry") {
    val tweet = Tweet.createTweet(jsonDocument)
    println(tweet)
  }
}
