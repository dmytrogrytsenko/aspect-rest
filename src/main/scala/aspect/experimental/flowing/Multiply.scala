package aspect.experimental.flowing

import akka.actor.{ActorRef, ActorContext, Props}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.common.flowing.Messages._
import aspect.common.flowing._

case class Multiply(name: String, underlying: ActorRef, input1: Input[Int], input2: Input[Int], output: Output[Int])
  extends Reactor with DefaultOutput[Int]

object Multiply {
  def create(implicit context: ActorContext) = {
    val name = this.getClass.getSimpleName
    val underlying = Props[MultiplyReactor].create(name)
    val input1 = Input[Int](name, "input1")
    val input2 = Input[Int](name, "input2")
    val output = Output.default[Int](name)
    Multiply(name, underlying, input1, input2, output)
  }
}

class MultiplyReactor extends BaseActor {
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
