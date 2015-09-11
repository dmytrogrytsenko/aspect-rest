package aspect.experimental.messaging

import akka.actor.{ActorContext, ActorRef, Props}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.experimental.messaging.Messages._

case class Exchange(underlying: ActorRef) extends Endpoint

object Exchange {
  def create(name: String)(implicit context: ActorContext): Exchange = {
    val underlying = ExchangeActor.props.create(name)
    Exchange(underlying)
  }
}

object ExchangeActor {
  def props = Props(classOf[ExchangeActor])
}

class ExchangeActor extends BaseActor {

  def receive = {
    case Start => become(idle)
  }

  def idle: Receive = {
    case Send(sendId, msg) => become(waitingForRequest(sender(), sendId, msg))
    case Request(requestId) => become(waitingForSend(sender(), requestId))
  }

  def waitingForRequest(source: ActorRef, sendId: CorrelationId, msg: Message): Receive = {
    case Request(requestId) => handle(source, sendId, msg, sender(), requestId)
    case Cancel(`sendId`) => become(idle)
  }

  def waitingForSend(destination: ActorRef, requestId: CorrelationId): Receive = {
    case Send(sendId, msg) => handle(sender(), sendId, msg , destination, requestId)
    case Cancel(`requestId`) => become(idle)
  }

  def handle(source: ActorRef, sendId: CorrelationId, msg: Message, destination: ActorRef, requestId: CorrelationId) = {
    destination ! Handle(requestId, msg)
    become(waitingForAccepted(source, sendId, requestId))
  }

  def waitingForAccepted(source: ActorRef, sendId: CorrelationId, requestId: CorrelationId): Receive = {
    case Accepted(`requestId`) =>
      //do nothing
    case Completed(`requestId`) =>
      source ! Acknowledge(sendId)
      become(idle)
    case Failed(`requestId`, e) =>
      source ! Failed(sendId, e)
      become(idle)
  }
}
