package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import Messages._

object Increment {
  def apply(name: String = "")(implicit context: ActorContext) =
    new Reactor(Props[Increment], name) with DefaultInput[Int] with DefaultOutput[Int] {
      val input = Input.default[Int]
      val output = Output.default[Int]
    }
}

class Increment extends BaseActor {
  val input = Endpoint.createDefaultInput
  val output = Endpoint.createDefaultOutput

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
      val value = msg.parts("value").asInstanceOf[Int]
      input !! Acknowledge(reqId)
      val result = value + 1
      val sndId = CorrelationId.generate
      output !! Send(sndId, Message.create("value" -> result))
      become(sending(sndId))
  }

  def sending(sndId: CorrelationId): Receive = {
    case Accepted(`sndId`) => request()
  }
}

