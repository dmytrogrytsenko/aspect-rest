package aspect.experimental.flowing

import akka.actor.{ActorContext, Props}
import aspect.common.Messages.Start
import aspect.common.actors.SingleUseActor
import aspect.common.flowing.Messages.Message
import aspect.common.flowing.Operation

object Increment {
  def create(implicit context: ActorContext) =
    Operation.create[Int, Int](msg => Props(classOf[Increment], msg))
}

class Increment(msg: Message) extends SingleUseActor {
  def receive = {
    case Start =>
      val value = msg.parts("value").asInstanceOf[Int]
      val result = value + 1
      complete(Message.create("value" -> result))
  }
}
