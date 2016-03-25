package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain._
import aspect.domain.twitter._
import org.joda.time.DateTime
import reactivemongo.api.DB
import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

object TwitterQueryCollection extends MongoCollection[TwitterQueryId, TwitterQuery] {
  val name = "twitterQueries"

  implicit object TwitterQueryIdReader extends BSONReader[BSONString, TwitterQueryId] {
    def read(bson: BSONString): TwitterQueryId = TwitterQueryId(bson.value)
  }

  implicit object TwitterQueryIdWriter extends BSONWriter[TwitterQueryId, BSONString] {
    def write(value: TwitterQueryId): BSONString = BSONString(value.underlying)
  }

  implicit object TwitterSearchRequestIdReader extends BSONReader[BSONString, TwitterSearchRequestId] {
    def read(bson: BSONString): TwitterSearchRequestId = TwitterSearchRequestId(bson.value)
  }

  implicit object TwitterSearchRequestIdWriter extends BSONWriter[TwitterSearchRequestId, BSONString] {
    def write(value: TwitterSearchRequestId): BSONString = BSONString(value.underlying)
  }

  implicit object TweetPointReader extends BSONDocumentReader[TweetPoint] {
    def read(doc: BSONDocument) = TweetPoint(
      id = doc.getAs[Long]("id").get,
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

  implicit object LastRequestReader extends BSONDocumentReader[LastRequest] {
    def read(doc: BSONDocument) = LastRequest(
      id = doc.getAs[TwitterSearchRequestId]("id").get,
      startTime = doc.getAs[DateTime]("startTime").get,
      finishTime = doc.getAs[DateTime]("finishTime").get,
      duration = doc.getAs[Duration]("duration").get,
      error = doc.getAs[LastError]("error"))
  }

  implicit object LastRequestWriter extends BSONDocumentWriter[LastRequest] {
    def write(value: LastRequest) = $doc(
      "id" -> value.id,
      "startTime" -> value.startTime,
      "finishTime" -> value.finishTime,
      "duration" -> value.duration,
      "error" -> value.error)
  }

  implicit object CurrentRequestReader extends BSONDocumentReader[CurrentRequest] {
    def read(doc: BSONDocument) = CurrentRequest(
      id = doc.getAs[TwitterSearchRequestId]("id").get,
      startTime = doc.getAs[DateTime]("startTime").get)
  }

  implicit object CurrentRequestWriter extends BSONDocumentWriter[CurrentRequest] {
    def write(value: CurrentRequest) = $doc(
      "id" -> value.id,
      "startTime" -> value.startTime)
  }

  implicit object ProcessingInfoReader extends BSONDocumentReader[ProcessingInfo] {
    def read(doc: BSONDocument) = ProcessingInfo(
      last = doc.getAs[LastRequest]("last"),
      current = doc.getAs[CurrentRequest]("current"),
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

  implicit object TwitterQueryReader extends BSONDocumentReader[TwitterQuery] {
    def read(doc: BSONDocument) = TwitterQuery(
      id = doc.getAs[TwitterQueryId]("_id").get,
      query = doc.getAs[String]("query").get,
      found = doc.getAs[TweetRange]("found"),
      pending = doc.getAs[TweetRange]("pending"),
      forward = doc.getAs[ProcessingInfo]("forward"),
      backward = doc.getAs[ProcessingInfo]("backward"),
      backwardCompleted = doc.getAs[Boolean]("backwardCompleted"),
      disabled = doc.getAs[Boolean]("disabled"),
      track = doc.getAs[TrackInfo]("track").get)
  }

  implicit object TwitterQueryWriter extends BSONDocumentWriter[TwitterQuery] {
    def write(value: TwitterQuery) = $doc(
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

  def enabledQueries(implicit db: DB, ec: ExecutionContext): Future[List[TwitterQuery]] =
    items
      .find($doc("disabled" $ne true))
      .cursor[TwitterQuery]
      .collect[List]()
}
