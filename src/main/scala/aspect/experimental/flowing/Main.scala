package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}

object Main {
  def apply(name: String = "")(implicit context: ActorContext) =
    new Reactor(Props[Main], name) { }
}

class Main extends FlowActor {
  val gen = Generate("generate")
  val inc = Increment("increment")
  val mul = Multiply("multiply")
  val print = Print("print")

  gen ~> inc ~> mul.input1 ; mul ~> print
  gen ~>        mul.input2
}
