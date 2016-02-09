package aspect.experimental.messaging

import aspect.common._
import org.joda.time.DateTime

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

  case class Message(id: MessageId,
                     createdAt: DateTime,
                     flowIds: Set[FlowId],
                     parts: Map[String, Any],
                     stack: List[Map[String, Any]]) {
    def as[T](name: String) = parts(name).asInstanceOf[T]
    def has[T](name: String) = parts.get(name).map(_.asInstanceOf[T]).contains(true)
  }

  object Message {
    def create(part: (String, Any)) =
    Message(
      id = MessageId.generate,
      createdAt = DateTime.now,
      flowIds = Set(FlowId.generate),
      parts = Map(part),
      stack = List.empty)
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
