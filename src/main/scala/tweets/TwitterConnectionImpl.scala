package tweets

import scalaj.http._
import scala.io.BufferedSource

class TwitterConnectionImpl extends TwitterConnection{

  //var observerList:ArrayBuffer[TweetListener] = ArrayBuffer()
  var tt:twitterThread= null
  //var running = new AtomicBoolean(true)
  //initialize

  class twitterThread extends Thread {

    override def run() {

      val TWITTER_FILTERED_STREAM_ENDPOINT = "https://api.twitter.com/2/tweets/search/stream?tweet.fields=created_at,entities&expansions=author_id,in_reply_to_user_id"
      val bearerToken= sys.env.getOrElse("TWITTER_BEARER","")
      while (running.get) {
        println("receiving tweets...")

        val request: HttpRequest =
          Http(TWITTER_FILTERED_STREAM_ENDPOINT)
            .header("Authorization", s"Bearer ${bearerToken}")
            .timeout(Integer.MAX_VALUE, Integer.MAX_VALUE)
        request.execute(is => {
          if (is != null) {
            val bufSource: BufferedSource = scala.io.Source.fromInputStream(is)
            val bufReader= bufSource.bufferedReader
            while(running.get) {

              // readln blocks until the next line could be read
              val line = bufReader.readLine

              println(line)
              sendTweetToListeningClasses(line)
              /*
              val tweetOpt = MyTweet.createTweet(line)
              tweetOpt match {
                case Some(tweet) => sendTweetToListeningClasses(tweet)
                case None => println("--No tweet created--")
              }
               */
            }
          }
          else {
            println(".... Restart Connection Necessary .....")
          }
        })
      }
    }
  }

  override def initialize:Unit={

    tt= new twitterThread
    tt.start
  }

  override def stop: Unit = {
    println("************called Twitter Stop*************")
    running.set(false)
  }
}

object TwitterConnectionImpl extends TwitterConnection {

  var twiCon:TwitterConnection= new TwitterConnectionImpl

  def createTwitterConnection:TwitterConnection={

    twiCon
  }

  override def stop={
    twiCon.stop
  }
}
