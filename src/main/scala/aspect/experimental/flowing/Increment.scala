package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}
import aspect.common.Messages.{Stop, Start}
import aspect.common.actors.BaseActor
import Messages._

object Increment {
  def apply(name: String = "")(implicit context: ActorContext) = {
    new Reactor(name) with DefaultInput[Int] with DefaultOutput[Int] {
      val props = Props[Increment]
      val input = Input.default[Int]
      val output = Output.default[Int]
    }
  }
}

class Increment extends BaseActor {
  val input = Endpoint.createDefaultInput
  val output = Endpoint.createDefaultOutput

  def receive = {
    case Start => request()
  }

  def request() = {
    val requestingId = CorrelationId.generate
    input ! Request(requestingId)
    become(requesting(requestingId))
  }

  def requesting(requestingId: CorrelationId): Receive = {
    case Handle(`requestingId`, message) =>
      val value = message.parts("value").asInstanceOf[Int]
      input ! Accepted(requestingId)
      val result = value + 1
      val sendingId = CorrelationId.generate
      output ! Send(sendingId, Message.create("value" -> result))
      become(sending(requestingId, sendingId))
    case Failed(`requestingId`, e) =>
      log.error(e, "Request failed.")
      request()
    case Stop =>
      input ! Cancel(requestingId)
      stop()
  }

  def sending(requestingId: CorrelationId, sendingId: CorrelationId): Receive = {
    case Acknowledge(`sendingId`) =>
      input ! Completed(requestingId)
      request()
    case Failed(`sendingId`, e) =>
      log.error(e, "Send failed.")
      input ! Failed(requestingId, e)
      request()
    case Cancel(`requestingId`) =>
      output ! Cancel(sendingId)
    case Stop =>
  }
}

