package aspect.processors

import akka.actor.Stash
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.domain.{TwitterRequestId, TwitterRequest}
import aspect.repositories.{UpdateTwitterRequest, EnabledTwitterRequests, GetEnabledTwitterRequests, TwitterRequestRepository}
import org.joda.time.DateTime

case object GetForwardRequest
case class ForwardRequest(request: Option[TwitterRequest])

case object GetBackwardRequest
case class BackwardRequest(request: Option[TwitterRequest])

class TwitterRequestSource extends BaseActor with Stash {
  private var requests: Map[TwitterRequestId, TwitterRequest] = Map.empty

  def receive = {
    case Start =>
      TwitterRequestRepository.endpoint !! GetEnabledTwitterRequests
    case EnabledTwitterRequests(items) =>
      this.requests = items.map(request => request.id -> request).toMap
      become(processing)
      unstashAll()
    case _ => stash()
  }

  def processing: Receive = {
    case GetForwardRequest =>
      getForwardRequest map { request =>
        val changedRequest = request
        changedRequest
      } map {
        updateRequest
      } pipe { request =>
        sender() !! ForwardRequest(request)
      }
    case GetBackwardRequest =>
      getBackwardRegular map { request =>
        val changedRequest = request
        changedRequest
      } map {
        updateRequest
      } pipe { request =>
        sender() !! BackwardRequest(request)
      }
  }

  def getForwardRequest: Option[TwitterRequest] =
    getForwardInitial orElse getForwardPending orElse getForwardRegular orElse getForwardOldest

  def getForwardInitial: Option[TwitterRequest] =
    requests
      .values
      .filter(_.isInitial)
      .toList
      .sortBy(_.track.createTime)
      .headOption

  def getForwardRegular: Option[TwitterRequest] =
    requests
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .filter(_.isForwardNextTimeHasCome)
      .toList
      .sortBy(_.forward.map(_.nextTime))
      .headOption

  def getForwardPending: Option[TwitterRequest] =
    requests
      .values
      .filterNot(_.isInitial)
      .filter(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .headOption

  def getForwardOldest: Option[TwitterRequest] =
    requests
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .headOption

  def getBackwardRegular: Option[TwitterRequest] =
    requests
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isBackwardCompleted)
      .filterNot(_.isBackwardExecuting)
      .filter(_.isBackwardNextTimeHasCome)
      .toList
      .sortBy(_.backward.map(_.nextTime))
      .headOption

  def updateRequest(request: TwitterRequest): TwitterRequest = {
    TwitterRequestRepository.endpoint !! UpdateTwitterRequest(request)
    this.requests += request.id -> request
    request
  }
}
