package aspect.processors

import akka.actor.Stash
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.domain.{GetTwitterRequest, TwitterQueryId, TwitterQuery}
import aspect.repositories.{UpdateTwitterQuery, EnabledTwitterQueries, GetEnabledTwitterQueries, TwitterQueryRepository}

class TwitterRequestSource extends BaseActor with Stash {
  private var queries: Map[TwitterQueryId, TwitterQuery] = Map.empty

  def receive = {
    case Start =>
      TwitterQueryRepository.endpoint !! GetEnabledTwitterQueries
    case EnabledTwitterQueries(items) =>
      this.queries = items.map(request => request.id -> request).toMap
      become(processing)
      unstashAll()
    case _ =>
      stash()
  }

  def processing: Receive = {
    case GetTwitterRequest => ???
  }

  def getForwardInitial: Option[TwitterQuery] =
    queries
      .values
      .filter(_.isInitial)
      .toList
      .sortBy(_.track.createTime)
      .headOption

  def getForwardRegular: Option[TwitterQuery] =
    queries
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .filter(_.isForwardNextTimeHasCome)
      .toList
      .sortBy(_.forward.map(_.nextTime))
      .headOption

  def getForwardPending: Option[TwitterQuery] =
    queries
      .values
      .filterNot(_.isInitial)
      .filter(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .headOption

  def getForwardOldest: Option[TwitterQuery] =
    queries
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .headOption

  def getBackwardRegular: Option[TwitterQuery] =
    queries
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isBackwardCompleted)
      .filterNot(_.isBackwardExecuting)
      .filter(_.isBackwardNextTimeHasCome)
      .toList
      .sortBy(_.backward.map(_.nextTime))
      .headOption

  def updateQuery(query: TwitterQuery): TwitterQuery = {
    TwitterQueryRepository.endpoint !! UpdateTwitterQuery(query)
    this.queries += query.id -> query
    query
  }
}
