package aspect.experimental.flowing

import akka.actor.{ActorRef, Props, ActorContext}
import aspect.common._

case class MainFlow(name: String, underlying: ActorRef) extends Reactor

object MainFlow {
  def create(implicit context: ActorContext) = {
    val name = this.getClass.getSimpleName
    val underlying = Props[MainFlowReactor].create(name)
    MainFlow(name, underlying)
  }
}

class MainFlowReactor extends FlowReactor {
  val gen = Generate.create
  val inc = Increment.create
  val mul = Multiply.create
  val print = Print.create

  gen ~> inc ~> mul.input1 ; mul ~> print
  gen ~>        mul.input2
}
