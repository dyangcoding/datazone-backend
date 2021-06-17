package streams

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.{ScalaReflection}
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.sources.v2.reader.streaming.{MicroBatchReader}
import org.apache.spark.sql.sources.v2.{DataSourceOptions, DataSourceV2, MicroBatchReadSupport}
import org.apache.spark.sql.types._
import tweets.Tweet

import java.util.Optional

class TwitterStreamingSource extends DataSourceV2 with MicroBatchReadSupport with DataSourceRegister with Logging {

  override def createMicroBatchReader(schema: Optional[StructType], checkpointLocation: String, options: DataSourceOptions): MicroBatchReader = {
    new TwitterStreamMicroBatchReader(options)
  }
  override def shortName(): String = "twitter"
}


object TwitterStreamingSource {
  val DEBUG_LEVEL = "debugLevel"
  val NUM_PARTITIONS = "numPartitions"
  val QUEUE_SIZE = "queueSize"
  val SCHEMA: StructType = ScalaReflection.schemaFor[Tweet].dataType.asInstanceOf[StructType]
}


