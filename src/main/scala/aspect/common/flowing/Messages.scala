package aspect.common.flowing

import aspect.common._

import scala.reflect.ClassTag

object Messages {
  case class RequestId(underlying: String)

  object RequestId {
    def generate = RequestId(newUUID)
  }

  case class PublishId(underlying: String)

  object PublishId {
    def generate = PublishId(newUUID)
  }

  case class FlowId(underlying: String)

  object FlowId {
    def generate = FlowId(newUUID)
  }

  case class MessageId(underlying: String)

  object MessageId {
    def generate = MessageId(newUUID)
  }

  case class Message(id: MessageId, flowIds: Set[FlowId], body: Any) {
    def as[T: ClassTag] = body.asInstanceOf[T]
    def is[T: ClassTag] = body.isInstanceOf[T]
  }

  object Message {
    def first(body: Any) = Message(MessageId.generate, Set(FlowId.generate), body)
  }

  case class Request(id: RequestId)
  case class RequestPending(id: RequestId)
  case class Handle(id: RequestId, msg: Message)
  case class HandlePending(id: RequestId)
  case class HandleCompleted(id: RequestId)
  case class HandleFailed(id: RequestId, exception: Throwable)

  case class Publish(id: PublishId, msg: Message)
  case class PublishPending(id: PublishId)
  case class PublishAccepted(id: PublishId)
  case class PublishFailed(id: PublishId, exception: Throwable)
}
