package utils

import scala.io.Source

object FileIO {
  def getToken: String = {
    val file = getClass.getResource("/twitterToken.txt").getFile
    val source = Source.fromFile(file)
    source.mkString
  }
}
