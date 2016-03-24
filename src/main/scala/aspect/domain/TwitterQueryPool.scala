package aspect.domain

import aspect.common._

object TwitterQueryPool {
  lazy val empty = new TwitterQueryPool(Map.empty)

  def apply(items: List[TwitterQuery]): TwitterQueryPool =
    TwitterQueryPool(items.map(request => request.id -> request).toMap)
}

case class TwitterQueryPool(items: Map[TwitterQueryId, TwitterQuery]) {
  def +(query: TwitterQuery) = copy(items = items + (query.id -> query))

  def nextPending: Option[TwitterQuery] =
    items
      .values
      .filterNot(_.isInitial)
      .filter(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .headOption

  def nextInitial: Option[TwitterQuery] =
    items
      .values
      .filter(_.isInitial)
      .toList
      .sortBy(_.track.createTime)
      .headOption

  def nextRegular: Option[TwitterQuery] =
    items
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .filter(_.isForwardNextTimeHasCome)
      .toList
      .sortBy(_.forward.map(_.nextTime))
      .headOption

  def nextRecent: Option[TwitterQuery] =
    items
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .reverse
      .headOption

  def nextOldest: Option[TwitterQuery] =
    items
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isPending)
      .filterNot(_.isForwardExecuting)
      .toList
      .sortBy(_.track.lastUpdateTime)
      .headOption

  def nextBackward: Option[TwitterQuery] =
    items
      .values
      .filterNot(_.isInitial)
      .filterNot(_.isBackwardCompleted)
      .filterNot(_.isBackwardExecuting)
      .filter(_.isBackwardNextTimeHasCome)
      .toList
      .sortBy(_.backward.map(_.nextTime))
      .headOption

}
