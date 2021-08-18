package utils

import scala.io.Source

object FileIO {
  def getToken: String = {
    val file = getClass.getResource("/twitterToken.txt").getFile
    val source = Source.fromFile(file)
    source.mkString
  }

  def getEndpoint: String = {
    val file = getClass.getResource("/twitterEndpoint.txt").getFile
    val source = Source.fromFile(file)
    source.mkString
  }

  def getMongoUri(debugMode: Boolean = true): String = {
    val fileName = if (debugMode) "/mongoUriDebug.txt" else "/mongoUri.txt"
    val file = getClass.getResource(fileName).getFile
    val source = Source.fromFile(file)
    source.mkString
  }
}
