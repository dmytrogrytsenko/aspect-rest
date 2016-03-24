package aspect.processors

import java.util.concurrent.ThreadLocalRandom

import akka.actor.Stash
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.domain._
import aspect.repositories._

class TwitterRequestSource extends BaseActor with Stash {
  private var queries = TwitterQueryPool.empty

  def receive = {
    case Start =>
      TwitterQueryRepository.endpoint !! GetEnabledTwitterQueries
    case EnabledTwitterQueries(items) =>
      this.queries = TwitterQueryPool(items)
      become(processing)
      unstashAll()
    case _ =>
      stash()
  }

  def processing: Receive = {
    case GetTwitterRequest =>
      handle.map(update).getOrElse(NoTwitterRequest).pipe(sender() !! _)
    case TwitterRequestCompleted(queryId, requestId, result) =>
      queries
        .items
        .get(queryId)
        .filter(query => query.forward.exists(_.current.exists(_.id == requestId)))
        .foreach { query =>

      }
    case TwitterRequestFailed(queryId, requestId, error) =>
      //
  }

  /*
            /-> Regular --> Recent --> Backward --> 0
    Pending --> Oldest --> Backward --> 0
            \-> Backward --> Regular --> Recent --> 0
  */
  def handle = handlePending orElse {
    selectRequestType match {
      case Regular => handleRegular orElse handleRecent orElse handleBackward
      case Oldest => handleOldest orElse handleBackward
      case Backward => handleBackward orElse handleRegular orElse handleRecent
    }
  }

  def handlePending = queries.nextPending
    .map(query => query -> TwitterRequest.pending(query))
    .map { case (query, request) => request -> query.startForwardRequest(request) }

  def handleRegular = queries.nextRegular
    .map(query => query -> TwitterRequest.forward(query))
    .map { case (query, request) => request -> query.startForwardRequest(request) }

  def handleRecent = queries.nextRecent
    .map(query => query -> TwitterRequest.forward(query))
    .map { case (query, request) => request -> query.startForwardRequest(request) }

  def handleOldest = queries.nextOldest
    .map(query => query -> TwitterRequest.forward(query))
    .map { case (query, request) => request -> query.startForwardRequest(request) }

  def handleBackward = queries.nextBackward
    .map(query => query -> TwitterRequest.backward(query))
    .map { case (query, request) => request -> query.startBackwardRequest(request) }

  def update(pair: (TwitterRequest, TwitterQuery)): TwitterRequest = {
    val (request, updatedQuery) = pair
    this.queries += updatedQuery
    TwitterQueryRepository.endpoint !! UpdateTwitterQuery(updatedQuery)
    request
  }

  sealed trait RequestType
  case object Regular extends RequestType
  case object Oldest extends RequestType
  case object Backward extends RequestType

  def selectRequestType = {
    def randomInt(bound: Int) = ThreadLocalRandom.current().nextInt(bound)
    val distribution = List(Regular -> 80, Oldest -> 90, Backward -> 100)
    val hit = randomInt(100)
    distribution.filterNot(_._2 < hit).headOption.getOrElse(Regular)
  }
}
