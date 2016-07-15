package aspect.experimental.flowing

import akka.actor.{ActorContext, Props}
import aspect.common.Messages.Start
import aspect.common.actors.Operation
import Messages.Message

object Increment {
  def create(implicit context: ActorContext) =
    Operation1.create[Int, Int](msg => Props(classOf[Increment], msg))
}

class Increment(msg: Message) extends Operation {
  def receive = {
    case Start =>
      val value = msg.as[Int]
      val result = value + 1
      complete(Message.create(result))
  }
}
