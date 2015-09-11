package aspect.experimental.messaging

import akka.actor.ActorRef
import aspect.common._

import scala.collection.immutable.Queue

object Messages {
  case class FlowId(underlying: String) extends AnyVal

  object FlowId {
    def generate = FlowId(newUUID)
  }

  case class MessageId(underlying: String) extends AnyVal

  object MessageId {
    def generate = MessageId(newUUID)
  }

  case class Message(id: MessageId, flowId: FlowId, body: Any)

  object Message {
    def create(body: Any) = Message(MessageId.generate, FlowId.generate, body)
    def derive(source: Message, body: Any) = Message(MessageId.generate, source.flowId, body)
  }

  case class CorrelationId(underlying: String) extends AnyVal

  object CorrelationId {
    def generate = CorrelationId(newUUID)
  }

  case class Send(id: CorrelationId, message: Message)
  case class SendMany(id: CorrelationId, messages: Queue[Message])
  case class Acknowledge(id: CorrelationId)
  case class Request(id: CorrelationId)
  case class RequestMany(id: CorrelationId, amount: Int)
  case class Cancel(id: CorrelationId)
  case class Handle(id: CorrelationId, message: Message)
  case class HandleMany(id: CorrelationId, messages: Queue[Message])
  case class Accepted(id: CorrelationId)
  case class Completed(id: CorrelationId)
  case class Failed(id: CorrelationId, e: Throwable)
  case class Pending(id: CorrelationId)

  case class LinkInput(endpoint: Endpoint)
  case class InputLinked(endpoint: Endpoint)
  case class LinkOutput(endpoint: Endpoint)
  case class OutputLinked(endpoint: Endpoint)
}
