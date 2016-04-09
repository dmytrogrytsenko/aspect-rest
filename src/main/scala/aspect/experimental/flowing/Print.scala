package aspect.experimental.flowing

import akka.actor.{ActorRef, Props, ActorContext}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import Messages._

case class Print(name: String, underlying: ActorRef, input: Input[Int])
  extends Reactor with DefaultInput[Int]

object Print {
  def create(implicit context: ActorContext) = {
    val name = this.getClass.getSimpleName
    val underlying = PrintReactor.props.create(name)
    val input = Input.default[Int](name)
    Print(name, underlying, input)
  }
}

object PrintReactor {
  def props = Props[PrintReactor]
}

class PrintReactor extends BaseActor {
  val input = Endpoint.createDefaultInput

  def receive = {
    case Start => request()
  }

  def request() = {
    val reqId = CorrelationId.generate
    input !! Request(reqId)
    become(requesting(reqId))
  }

  def requesting(reqId: CorrelationId): Receive = {
    case Handle(`reqId`, msg) =>
      input !! Acknowledge(reqId)
      handle(msg)
      request()
  }

  def handle(msg: Message) = {
    val value = msg.parts("value").asInstanceOf[Int]
    log.info(s"Value: $value")
    Thread.sleep(1000)
  }
}
