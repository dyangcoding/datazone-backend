package streams

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.v2.DataSourceOptions
import org.apache.spark.sql.sources.v2.reader.InputPartition
import org.apache.spark.sql.sources.v2.reader.streaming.{MicroBatchReader, Offset}
import org.apache.spark.sql.types.StructType
import tweets.{Tweet, TweetListener, TwitterConnection, TwitterConnectionImpl, TwitterConnectionMock}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import java.util.Optional
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, TimeUnit}

//quick and dirty data class
class Data(var tweetList: ListBuffer[Tweet], var currentOffset: TwitterOffset, var incomingEventCounter: Int) {
}

object Data {
  def create(): Data = new Data(new ListBuffer[Tweet](), new TwitterOffset(-1), 0)
}

class TwitterStreamMicroBatchReader(options: DataSourceOptions) extends MicroBatchReader with Logging {

  println("********************************************************")
  println("TwitterStream MicroBatchReader erzeugt!")
  println("********************************************************")

  class workerThread(queue: BlockingQueue[String], data: Data) extends Thread {
    println("********************************************************")
    println("Worker created")
    println("********************************************************")

    override def run() {

      println("********************************************************")
      println("Worker Run Method called")
      println("********************************************************")

      while (!stopped) {
        try {
          val tweet: String = queue.poll(100, TimeUnit.MILLISECONDS)
          if (tweet != null) {
            Tweet.createTweet(tweet) match {
              case Some(t) =>
                data.tweetList.append(t)
                data.currentOffset = data.currentOffset + 1
                data.incomingEventCounter = data.incomingEventCounter + 1;
              case None => internalLog("Can not create tweet from Input Source.")
            }
            // 1000 ms no tweet received. this is fine, maybe nobody tweeted that quickly
          }
          else print("-" + queue.size + "-")
        } catch {
          case exception :Exception => internalLog(exception.getMessage)
        }
      }
      println("********************************************************")
      println("Worker Thread gestoppt")
      println("********************************************************")
    }
  }

  // Possible Options
  private val numPartitions = options.get(TwitterStreamingSource.NUM_PARTITIONS).orElse("5").toInt
  private val queueSize = options.get(TwitterStreamingSource.QUEUE_SIZE).orElse("512").toInt
  private val debugLevel = options.get(TwitterStreamingSource.DEBUG_LEVEL).orElse("debug").toLowerCase

  // Initialize offsets and parameter for creating MicroBatches
  private val NO_DATA_OFFSET = TwitterOffset(-1)
  private var startOffset: TwitterOffset = new TwitterOffset(-1)
  private var endOffset: TwitterOffset = new TwitterOffset(-1)

  private var lastReturnedOffset: TwitterOffset = new TwitterOffset(-2)
  private var lastOffsetCommitted: TwitterOffset = new TwitterOffset(-1)

  private var stopped: Boolean = false

  private var worker: workerThread = _

  private val data: Data = Data.create();
  private var tweetQueue: ArrayBlockingQueue[String] = _

  private var twitterCon:TwitterConnection= _
  private var tl:TweetListener= _

  initialize()

  def initialize(): Unit = synchronized {

    tweetQueue = new ArrayBlockingQueue(queueSize)
    worker = new workerThread(tweetQueue, data)
    worker.start()

    twitterCon = TwitterConnectionImpl.createTwitterConnection
    tl = (tweet: String) => tweetQueue.add(tweet)
    twitterCon.registerEventListener(tl)
  }

  override def planInputPartitions(): java.util.List[InputPartition[InternalRow]] = {
    synchronized {
      val startOrdinal = startOffset.offset.toInt + 1
      val endOrdinal = endOffset.offset.toInt + 1
      internalLog(s"createDataReaderFactories: sOrd: $startOrdinal, eOrd: $endOrdinal, " +
        s"lastOffsetCommitted: $lastOffsetCommitted")
      val newBlocks = synchronized {
        val sliceStart = startOrdinal - lastOffsetCommitted.offset.toInt - 1
        val sliceEnd = endOrdinal - lastOffsetCommitted.offset.toInt - 1
        assert(sliceStart <= sliceEnd, s"sliceStart: $sliceStart sliceEnd: $sliceEnd")
        data.tweetList.slice(sliceStart, sliceEnd)
      }
      val result = newBlocks.grouped(numPartitions).map { block =>
        new TweetStreamBatchTask(block).asInstanceOf[InputPartition[InternalRow]]
      }.toList.asJava
      result
    }
  }

  override def setOffsetRange(start: Optional[Offset], end: Optional[Offset]): Unit = {
    if (start.isPresent && start.get().asInstanceOf[TwitterOffset].offset != data.currentOffset.offset) {
      internalLog(s"setOffsetRange: start: $start, end: $end currentOffset: ${data.currentOffset}")
    }
    this.startOffset = start.orElse(NO_DATA_OFFSET).asInstanceOf[TwitterOffset]
    this.endOffset = end.orElse(data.currentOffset).asInstanceOf[TwitterOffset]
  }

  override def getStartOffset: Offset = {
    internalLog("getStartOffset was called")
    if (startOffset.offset == -1) {
      throw new IllegalStateException("startOffset is -1")
    }
    startOffset
  }

  override def getEndOffset: Offset = {
    if (endOffset.offset == -1) {
      data.currentOffset
    } else {
      if (lastReturnedOffset.offset < endOffset.offset) {
        internalLog(s"** getEndOffset => $endOffset)")
        lastReturnedOffset = endOffset
      }
      endOffset
    }
  }

  override def commit(end: Offset): Unit = {
    internalLog(s"** commit($end) lastOffsetCommitted: $lastOffsetCommitted")
    val newOffset = TwitterOffset.convert(end).getOrElse(
      sys.error(s"TwitterStreamMicroBatchReader.commit() received an offset ($end) that did not " +
        s"originate with an instance of this class")
    )
    val offsetDiff = (newOffset.offset - lastOffsetCommitted.offset).toInt
    if (offsetDiff < 0) {
      sys.error(s"Offsets committed out of order: $lastOffsetCommitted followed by $end")
    }
    data.tweetList.trimStart(offsetDiff)
    lastOffsetCommitted = newOffset
  }

  override def stop(): Unit = {
    log.warn(s"There is a total of ${data.incomingEventCounter} events that came in")
    twitterCon.removeEventListener(tl)
    stopped = true
  }

  override def deserializeOffset(json: String): Offset = {
    TwitterOffset(json.toLong)
  }

  override def readSchema(): StructType = {
    TwitterStreamingSource.SCHEMA
  }

  private def internalLog(msg: String): Unit = {
    debugLevel match {
      case "warn" => log.warn(msg)
      case "info" => log.info(msg)
      case "debug" => log.debug(msg)
      case _ =>
    }
  }
}