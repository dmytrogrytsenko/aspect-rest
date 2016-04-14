package aspect.domain.twitter

import aspect.domain.TrackInfo
import org.joda.time.DateTime

import scala.concurrent.duration.Duration

case class TwitterQueryId(underlying: String) extends AnyVal

case class TweetPoint(id: Long, time: DateTime)
case class TweetRange(min: TweetPoint, max: TweetPoint)

case class LastError(count: Int, message: String)

case class LastRequest(id: TwitterSearchRequestId,
                       startTime: DateTime,
                       finishTime: DateTime,
                       duration: Duration,
                       error: Option[LastError])

case class CurrentRequest(id: TwitterSearchRequestId, startTime: DateTime)

object CurrentRequest {
  def create = CurrentRequest(TwitterSearchRequestId.generate, DateTime.now)
}

case class ProcessingInfo(last: Option[LastRequest],
                          current: Option[CurrentRequest],
                          nextTime: DateTime,
                          successInterval: Duration,
                          errorInterval: Duration) {
  def executing = current.isDefined
  def currentRequestId = current.map(_.id)
}

case class TwitterQuery(id: TwitterQueryId,
                        query: String,
                        found: Option[TweetRange],
                        pending: Option[TweetRange],
                        forward: Option[ProcessingInfo],
                        backward: Option[ProcessingInfo],
                        backwardCompleted: Option[Boolean],
                        disabled: Option[Boolean],
                        track: TrackInfo) {

  def isInitial = forward.isEmpty
  def isPending = pending.isEmpty
  def isForwardExecuting = forward.exists(_.current.isEmpty)
  def isBackwardExecuting = backward.exists(_.current.isEmpty)
  def isBackwardCompleted = backwardCompleted.contains(true)

  def startForwardRequest: (TwitterSearchRequest, TwitterQuery) = {
    val updatedQuery = copy(
      forward = Some(forward.get.copy(current = Some(CurrentRequest.create))),
      track = track.copy(version = track.version + 1, lastUpdateTime = DateTime.now))
    val request = if (isPending) TwitterSearchRequest.pending(this) else TwitterSearchRequest.forward(this)
    (request, updatedQuery)
  }

  def startBackwardRequest: (TwitterSearchRequest, TwitterQuery) = {
    val updatedQuery = copy(
      backward = Some(backward.get.copy(current = Some(CurrentRequest.create))),
      track = track.copy(version = track.version + 1, lastUpdateTime = DateTime.now))
    val request = TwitterSearchRequest.backward(this)
    (request, updatedQuery)
  }

  def completeForwardRequest(result: TwitterSearchResponse): TwitterQuery = ???

  def completeBackwardRequest(result: TwitterSearchResponse): TwitterQuery = ???
}

