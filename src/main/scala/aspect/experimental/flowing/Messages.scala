package aspect.experimental.flowing

import aspect.common._

import scala.collection.immutable.Queue

object Messages {
  case class FlowId(underlying: String)

  object FlowId {
    def generate = FlowId(newUUID)
  }

  case class MessageId(underlying: String)

  object MessageId {
    def generate = MessageId(newUUID)
  }

  case class Message(id: MessageId, flowIds: Set[FlowId], body: Any) {
    def as[T] = body.asInstanceOf[T]
    def is[T] = body.isInstanceOf[T]
  }

  object Message {
    def create(body: Any) = Message(MessageId.generate, Set(FlowId.generate), body)
  }

  case class CorrelationId(underlying: String)

  object CorrelationId {
    def generate = CorrelationId(newUUID)
  }

  case class Send(id: CorrelationId, message: Message)
  case class SendMany(id: CorrelationId, messages: Queue[Message])
  case class Accepted(id: CorrelationId)

  case class Request(id: CorrelationId)
  case class RequestMany(id: CorrelationId, amount: Int)
  case class Handle(id: CorrelationId, message: Message)
  case class HandleMany(id: CorrelationId, messages: Queue[Message])
  case class Acknowledge(id: CorrelationId)

  case class GetStatus(id: CorrelationId)
  case class Pending(id: CorrelationId)
  case class Failed(id: CorrelationId, error: Throwable)
}
