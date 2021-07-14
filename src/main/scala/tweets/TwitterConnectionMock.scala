package tweets

import scala.io.BufferedSource

class TwitterConnectionMock extends TwitterConnection{
  var tt:twitterThread= _
  var bufSource: BufferedSource = _
  val TWEET_DELAY_IN_MS= 100

  class twitterThread extends Thread {
    override def run() {
      Thread.sleep(2000)
      val bufReader= bufSource.bufferedReader
      while(running.get) {
        val line = bufReader.readLine
        if (line == null) running.set(false)
        else {
          Thread.sleep(TWEET_DELAY_IN_MS)
          synchronized {sendTweetToListeningClasses(line)}
        }
      }
    }
  }

  override def initialize:Unit={
    bufSource= scala.io.Source.fromFile("twitterResponseDemo.json")
    tt= new twitterThread
    tt.start
  }

  override def stop: Unit = {
    println("************called Twitter Stop*************")
    running.set(false)
  }
}

object TwitterConnectionMock extends TwitterConnection {
  var twiCon:TwitterConnection= new TwitterConnectionMock
  def createTwitterConnection:TwitterConnection={
    twiCon
  }

  override def stop={
    twiCon.stop
  }
}
