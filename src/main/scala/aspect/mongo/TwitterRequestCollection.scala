package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain._
import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

object TwitterRequestCollection extends MongoCollection[TwitterRequestId, TwitterRequest] {
  val name = "twitterRequests"

  implicit object TwitterRequestIdReader extends BSONReader[BSONString, TwitterRequestId] {
    def read(bson: BSONString): TwitterRequestId = TwitterRequestId(bson.value)
  }

  implicit object TwitterRequestIdWriter extends BSONWriter[TwitterRequestId, BSONString] {
    def write(value: TwitterRequestId): BSONString = BSONString(value.underlying)
  }

  implicit object TwitterExecutionIdReader extends BSONReader[BSONString, TwitterExecutionId] {
    def read(bson: BSONString): TwitterExecutionId = TwitterExecutionId(bson.value)
  }

  implicit object TwitterExecutionIdWriter extends BSONWriter[TwitterExecutionId, BSONString] {
    def write(value: TwitterExecutionId): BSONString = BSONString(value.underlying)
  }

  implicit object TweetPointReader extends BSONDocumentReader[TweetPoint] {
    def read(doc: BSONDocument) = TweetPoint(
      id = doc.getAs[String]("id").get,
      time = doc.getAs[DateTime]("time").get)
  }

  implicit object TweetPointWriter extends BSONDocumentWriter[TweetPoint] {
    def write(value: TweetPoint) = $doc(
      "id" -> value.id,
      "time" -> value.time)
  }

  implicit object TweetRangeReader extends BSONDocumentReader[TweetRange] {
    def read(doc: BSONDocument) = TweetRange(
      min = doc.getAs[TweetPoint]("min").get,
      max = doc.getAs[TweetPoint]("max").get)
  }

  implicit object TweetRangeWriter extends BSONDocumentWriter[TweetRange] {
    def write(value: TweetRange) = $doc(
      "min" -> value.min,
      "max" -> value.max)
  }

  implicit object LastErrorReader extends BSONDocumentReader[LastError] {
    def read(doc: BSONDocument) = LastError(
      count = doc.getAs[Int]("count").get,
      message = doc.getAs[String]("message").get)
  }

  implicit object LastErrorWriter extends BSONDocumentWriter[LastError] {
    def write(value: LastError) = $doc(
      "count" -> value.count,
      "message" -> value.message)
  }

  implicit object LastExecutionReader extends BSONDocumentReader[LastExecution] {
    def read(doc: BSONDocument) = LastExecution(
      id = doc.getAs[TwitterExecutionId]("id").get,
      startTime = doc.getAs[DateTime]("startTime").get,
      finishTime = doc.getAs[DateTime]("finishTime").get,
      duration = doc.getAs[Duration]("duration").get,
      error = doc.getAs[LastError]("error"))
  }

  implicit object LastExecutionWriter extends BSONDocumentWriter[LastExecution] {
    def write(value: LastExecution) = $doc(
      "id" -> value.id,
      "startTime" -> value.startTime,
      "finishTime" -> value.finishTime,
      "duration" -> value.duration,
      "error" -> value.error)
  }

  implicit object CurrentExecutionReader extends BSONDocumentReader[CurrentExecution] {
    def read(doc: BSONDocument) = CurrentExecution(
      id = doc.getAs[TwitterExecutionId]("id").get,
      startTime = doc.getAs[DateTime]("startTime").get)
  }

  implicit object CurrentExecutionWriter extends BSONDocumentWriter[CurrentExecution] {
    def write(value: CurrentExecution) = $doc(
      "id" -> value.id,
      "startTime" -> value.startTime)
  }

  implicit object ProcessingInfoReader extends BSONDocumentReader[ProcessingInfo] {
    def read(doc: BSONDocument) = ProcessingInfo(
      last = doc.getAs[LastExecution]("last"),
      current = doc.getAs[CurrentExecution]("current"),
      nextTime = doc.getAs[DateTime]("nextTime").get,
      successInterval = doc.getAs[Duration]("successInterval").get,
      errorInterval = doc.getAs[Duration]("errorInterval").get)
  }

  implicit object ProcessingInfoWriter extends BSONDocumentWriter[ProcessingInfo] {
    def write(value: ProcessingInfo) = $doc(
      "last" -> value.last,
      "current" -> value.current,
      "nextTime" -> value.nextTime,
      "successInterval" -> value.successInterval,
      "errorInterval" -> value.errorInterval)
  }

  implicit object TrackInfoReader extends BSONDocumentReader[TrackInfo] {
    def read(doc: BSONDocument) = TrackInfo(
      version = doc.getAs[Long]("version").get,
      createTime = doc.getAs[DateTime]("createTime").get,
      lastUpdateTime = doc.getAs[DateTime]("lastUpdateTime").get)
  }

  implicit object TrackInfoWriter extends BSONDocumentWriter[TrackInfo] {
    def write(value: TrackInfo) = $doc(
      "version" -> value.version,
      "createTime" -> value.createTime,
      "lastUpdateTime" -> value.lastUpdateTime)
  }

  implicit object TwitterRequestReader extends BSONDocumentReader[TwitterRequest] {
    def read(doc: BSONDocument) = TwitterRequest(
      id = doc.getAs[TwitterRequestId]("_id").get,
      query = doc.getAs[String]("query").get,
      found = doc.getAs[TweetRange]("found"),
      pending = doc.getAs[TweetRange]("pending"),
      forward = doc.getAs[ProcessingInfo]("forward"),
      backward = doc.getAs[ProcessingInfo]("backward"),
      backwardCompleted = doc.getAs[Boolean]("backwardCompleted"),
      disabled = doc.getAs[Boolean]("disabled"),
      track = doc.getAs[TrackInfo]("track").get)
  }

  implicit object TwitterRequestWriter extends BSONDocumentWriter[TwitterRequest] {
    def write(value: TwitterRequest) = $doc(
      "_id" -> value.id,
      "query" -> value.query,
      "found" -> value.found,
      "pending" -> value.pending,
      "forward" -> value.forward,
      "backward" -> value.backward,
      "backwardCompleted" -> value.backwardCompleted,
      "disabled" -> value.disabled,
      "track" -> value.track)
  }

  def getForwardRequest: Future[Option[TwitterRequest]] = ???
}
