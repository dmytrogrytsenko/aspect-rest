package aspect.processors

import java.util.concurrent.ThreadLocalRandom

import akka.actor.Stash
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.domain.twitter.TwitterSearchBalanceRoute.{Adaptive, Oldest, Backward}
import aspect.domain.twitter.{TwitterQuery, TwitterSearchResponse, TwitterSearchRequestId, TwitterQueryPool}
import aspect.repositories._

case object GetTwitterSearchRequest
case object NoTwitterSearchRequest
case class TwitterSearchRequestFailed(requestId: TwitterSearchRequestId, error: String)

class TwitterRequestSource extends BaseActor with Stash {
  private val settings = TwitterSearchSettings(context.system)
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
    case GetTwitterSearchRequest =>
      startRequest.getOrElse(NoTwitterSearchRequest).pipe(sender() !! _)
    case response: TwitterSearchResponse => completeRequest(response)
    case TwitterSearchRequestFailed(requestId, error) => ???
  }

  def completeRequest(response: TwitterSearchResponse) = {
    lazy val forward = queries.findForward(response.requestId).map(_.completeForwardRequest(response))
    lazy val backward = queries.findBackward(response.requestId).map(_.completeBackwardRequest(response))
    forward orElse backward foreach { updatedQuery =>
      this.queries += updatedQuery
      TwitterQueryRepository.endpoint !! UpdateTwitterQuery(updatedQuery)
    }
  }

  /*
            /-> ForwardAdaptive --> Backward
    Pending --> ForwardOldest --> Backward
            \-> Backward --> ForwardAdaptive
  */
  def startRequest =
    handlePending orElse {
      selectBalanceRoute match {
        case Adaptive => handleAdaptive orElse handleBackward
        case Oldest => handleOldest orElse handleBackward
        case Backward => handleBackward orElse handleAdaptive
      }
    } map { case (request, updatedQuery) =>
      this.queries += updatedQuery
      TwitterQueryRepository.endpoint !! UpdateTwitterQuery(updatedQuery)
      request
    }

  def handlePending = queries.nextPending.map(_.startForwardRequest)
  def handleAdaptive = queries.nextAdaptive.map(_.startForwardRequest)
  def handleOldest = queries.nextOldest.map(_.startForwardRequest)
  def handleBackward = queries.nextBackward.map(_.startBackwardRequest)

  def selectBalanceRoute =
    randomInt(3) match {
      case 0 => Adaptive
      case 1 => Oldest
      case 2 => Backward
    }
}
