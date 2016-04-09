package aspect.experimental.flowing

import akka.actor.{ActorSelection, Props}
import aspect.common.Messages.Start
import aspect.common._
import aspect.common.actors.BaseActor
import aspect.experimental.flowing.Messages._

object Link {
  def props(source: ActorSelection, destination: ActorSelection): Props =
    Props(classOf[Link], source, destination)
}

class Link(source: ActorSelection, destination: ActorSelection) extends BaseActor {
  def receive = {
    case Start => request()
  }

  def request() = {
    val reqId = CorrelationId.generate
    source !! Request(reqId)
    become(requesting(reqId))
  }

  def requesting(reqId: CorrelationId): Receive = {
    case Handle(`reqId`, message) =>
      source !! Acknowledge(reqId)
      val sndId = CorrelationId.generate
      destination !! Send(sndId, message)
      become(sending(sndId))
  }

  def sending(sndId: CorrelationId): Receive = {
    case Accepted(`sndId`) => request()
  }
}
