package aspect.experimental.flowing

import akka.actor.{ActorContext, Props}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.experimental.flowing.Messages._

object Multiply {
  def apply(name: String = "")(implicit context: ActorContext) =
    new Reactor(Props[Multiply], name) with DefaultOutput[Int] {
      val input1 = Input[Int]("input1")
      val input2 = Input[Int]("input2")
      val output = Output.default[Int]
    }
}

class Multiply extends BaseActor {
  val input1 = Endpoint.createInput("input1")
  val input2 = Endpoint.createInput("input2")
  val output = Endpoint.createDefaultOutput

  def receive = {
    case Start => request()
  }

  def request() = {
    val reqId1 = CorrelationId.generate
    val reqId2 = CorrelationId.generate
    input1 !! Request(reqId1)
    input2 !! Request(reqId2)
    become(requesting(reqId1, reqId2, null, null))
  }

  def requesting(reqId1: CorrelationId, reqId2: CorrelationId, msg1: Message, msg2: Message): Receive = {
    case Handle(`reqId1`, msg) =>
      input1 !! Acknowledge(reqId1)
      if (msg2 != null) handle(msg, msg2)
      else become(requesting(reqId1, reqId2, msg, null))
    case Handle(`reqId2`, msg) =>
      input2 !! Acknowledge(reqId2)
      if (msg1 != null) handle(msg1, msg)
      else become(requesting(reqId1, reqId2, null, msg))
  }

  def handle(msg1: Message, msg2: Message) = {
    val value1 = msg1.parts("value").asInstanceOf[Int]
    val value2 = msg2.parts("value").asInstanceOf[Int]
    val result = value1 * value2
    val sndId = CorrelationId.generate
    output !! Send(sndId, Message.create("value" -> result))
    become(sending(sndId))
  }

  def sending(sndId: CorrelationId): Receive = {
    case Accepted(`sndId`) => request()
  }
}
