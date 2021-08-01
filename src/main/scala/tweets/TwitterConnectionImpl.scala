package tweets

import com.typesafe.scalalogging.Logger
import scalaj.http._
import utils.FileIO

import scala.io.BufferedSource

class TwitterConnectionImpl extends TwitterConnection{
  var tt:twitterThread= _
  val logger: Logger = Logger(classOf[TwitterConnectionImpl])
  class twitterThread extends Thread {
    override def run() {
      val TWITTER_FILTERED_STREAM_ENDPOINT = FileIO.getEndpoint
      val bearerToken= FileIO.getToken
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
              val line = bufReader.readLine
              if (line.nonEmpty) {
                sendTweetToListeningClasses(line)
              } else {
                logger.info("Read empty line from Input Source.")
              }
            }
          }
          else {
            logger.info(".... Restart Connection Necessary .....")
          }
        })
      }
    }
  }

  override def initialize:Unit={
    tt = new twitterThread
    tt.start()
  }

  override def stop: Unit = {
    println("************called Twitter Stop*************")
    running.set(false)
  }
}

object TwitterConnectionImpl extends TwitterConnection {
  var twiCon:TwitterConnection= new TwitterConnectionImpl
  def createTwitterConnection:TwitterConnection = twiCon
  override def stop: Unit = twiCon.stop
}
