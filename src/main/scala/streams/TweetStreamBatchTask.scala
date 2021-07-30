package streams

import org.apache.spark.sql.Encoder
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.v2.reader.{InputPartition, InputPartitionReader}
import tweets.Tweet

import scala.collection.mutable.ListBuffer

class TweetStreamBatchTask(tweetList:ListBuffer[Tweet]) extends InputPartition[InternalRow] {
  override def createPartitionReader(): InputPartitionReader[InternalRow] = new TweetStreamBatchReader(tweetList)
}

/*
 TweetStreamBatchReader liest Daten eines Batches
 hat intern einen Zeiger currentIdx
 Implementiert Iterator
 */
class TweetStreamBatchReader(tweetList:ListBuffer[Tweet]) extends InputPartitionReader[InternalRow] {
  private var currentIdx = -1
  import org.apache.spark.sql.Encoders
  val tweetEncoder: Encoder[Tweet] = Encoders.product[Tweet]
  import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
  val tweetExprEncoder: ExpressionEncoder[Tweet] = tweetEncoder.asInstanceOf[ExpressionEncoder[Tweet]]

  override def next(): Boolean = {
    currentIdx += 1
    currentIdx < tweetList.size
  }

  override def get(): InternalRow = {
    val tweet = tweetList(currentIdx)
    tweetExprEncoder.toRow(tweet)
  }

  def close(): Unit = {}
}

