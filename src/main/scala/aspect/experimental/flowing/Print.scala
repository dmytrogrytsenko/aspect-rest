package aspect.experimental.flowing

import akka.actor.{ActorContext, Props}
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor

object Print {
  def apply(name: String = "")(implicit context: ActorContext) = {
    new Reactor(name) with DefaultInput[Int] {
      val props = Props[Print]
      val input = Input.default[Int]
    }
  }
}

class Print extends BaseActor {
  val input = Endpoint.createDefaultInput

  def receive = {
    case Start =>
  }
}