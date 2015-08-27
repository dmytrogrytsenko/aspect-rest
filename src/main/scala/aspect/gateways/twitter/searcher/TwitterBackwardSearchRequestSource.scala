package aspect.gateways.twitter.searcher

import java.util.concurrent.TimeoutException

import akka.actor.{ReceiveTimeout, ActorRef}
import aspect.common._
import aspect.common.actors.{NodeSingleton, BaseActor}
import aspect.common.Messages.Start
import aspect.experimental.{Pending, Accepted, Send, Message}
import org.joda.time.DateTime

import scala.collection.immutable.Queue
import scala.concurrent.duration._

object TwitterSearchRequestRepository extends NodeSingleton[TwitterSearchRequestRepository]

class TwitterSearchRequestRepository extends BaseActor {
  def receive = ???
}

case class BackwardSearchRequest(id: String, query: String, minTweetId: Long, minTweetTime: DateTime)
case class GetBackwardSearchRequests(limit: Int)
case class BackwardSearchRequests(requests: List[BackwardSearchRequest])

class TwitterBackwardSearchRequestSource(output: ActorRef) extends BaseActor {
  val batchSize = 8
  val idleInterval = 15.seconds
  val receiveTimeout = 1.minute

  context.setReceiveTimeout(receiveTimeout)

  override def aroundReceive(body: Receive, msg: Any) = msg match {
    case ReceiveTimeout => throw new TimeoutException()
    case _ => super.aroundReceive(body, msg)
  }

  def receive = waitingForStart

  def waitingForStart: Receive = {
    case Start =>
      TwitterSearchRequestRepository.endpoint ! GetBackwardSearchRequests(batchSize)
      become(waitingForRequests)
  }

  def waitingForRequests: Receive = {
    case BackwardSearchRequests(Nil) =>
      scheduleOnce(idleInterval, Start)
      become(waitingForStart)
    case BackwardSearchRequests(requests) =>
      val id = newUUID
      val messages = requests.map(request => Message(newUUID, Map("default" -> request)))
      output ! Send(id, Queue(messages: _*))
      become(waitingForAccepted(id))
  }

  def waitingForAccepted(id: String): Receive = {
    case Accepted(`id`) =>
      self ! Start
      become(waitingForStart)
    case Pending(`id`) =>
  }
}
