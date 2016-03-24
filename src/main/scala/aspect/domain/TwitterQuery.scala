package aspect.domain

import aspect.common._
import org.joda.time.DateTime

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
  def isForwardNextTimeHasCome = forward.exists(_.nextTime <= DateTime.now)
  def isBackwardNextTimeHasCome = backward.exists(_.nextTime <= DateTime.now)

  def startForwardRequest(request: TwitterRequest): TwitterQuery = copy(
    forward = Some(forward.get.copy(current = Some(CurrentRequest(request.id, request.startTime)))),
    track = track.copy(version = track.version + 1, lastUpdateTime = DateTime.now))

  def startBackwardRequest(request: TwitterRequest): TwitterQuery = copy(
    backward = Some(backward.get.copy(current = Some(CurrentRequest(request.id, request.startTime)))),
    track = track.copy(version = track.version + 1, lastUpdateTime = DateTime.now))
}

