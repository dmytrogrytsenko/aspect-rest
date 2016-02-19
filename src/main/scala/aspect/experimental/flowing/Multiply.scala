package aspect.experimental.flowing

import akka.actor.{ActorContext, Props}
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor

object Multiply {
  def apply(name: String = "")(implicit context: ActorContext) = {
    new Reactor(name) with DefaultOutput[Int] {
      val props = Props[Multiply]
      val input1 = Input[Int]("input1")
      val input2 = Input[Int]("input2")
      val output = Output.default[Int]
    }
  }
}

class Multiply extends BaseActor {
  val input1 = Endpoint.createInput("input1")
  val input2 = Endpoint.createInput("input2")
  val output = Endpoint.createDefaultOutput

  def receive = {
    case Start =>
  }
}
