package aspect.experimental.flowing

import akka.actor.{ActorRef, ActorContext, Props}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.experimental.flowing.Messages._

import scala.collection.mutable

object Endpoint {
  def props = Props[Endpoint]
  def create(actorName: String)(implicit context: ActorContext) = Endpoint.props.create(actorName)
  def createInput(name: String)(implicit context: ActorContext) = create(s"inputs.$name")
  def createOutput(name: String)(implicit context: ActorContext) = create(s"outputs.$name")
  def createDefaultInput(implicit context: ActorContext) = createInput("default")
  def createDefaultOutput(implicit context: ActorContext) = createOutput("default")
}

class Endpoint extends BaseActor {
  private val snds = mutable.Queue.empty[(CorrelationId, Message, ActorRef)]
  private val reqs = mutable.Queue.empty[(CorrelationId, ActorRef)]

  def receive = {
    case Start => become(idle)
  }

  def idle: Receive = {
    case Send(id, msg) =>
      snds.enqueue((id, msg, sender()))
      process()
    case Request(id) =>
      reqs.enqueue((id, sender()))
      process()
  }

  def process() = {
    if (snds.nonEmpty && reqs.nonEmpty) {
      val (sndId, msg, source) = snds.dequeue()
      val (reqId, destination) = reqs.dequeue()
      destination !! Handle(reqId, msg)
      become(handling(sndId, source, reqId, destination))
    }
  }

  def handling(sndId: CorrelationId, source: ActorRef, reqId: CorrelationId, destination: ActorRef): Receive = {
    case Send(id, msg) =>
      snds.enqueue((id, msg, sender()))
    case Request(id) =>
      reqs.enqueue((id, sender()))
    case Acknowledge(`reqId`) =>
      source !! Accepted(sndId)
      become(idle)
      process()
  }
}

