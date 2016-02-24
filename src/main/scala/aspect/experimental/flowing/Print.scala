package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.experimental.flowing.Messages._

object Print {
  def apply(name: String = "")(implicit context: ActorContext) =
    new Reactor(Props[Print], name) with DefaultInput[Int] {
      val input = Input.default[Int]
  }
}

class Print extends BaseActor {
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
