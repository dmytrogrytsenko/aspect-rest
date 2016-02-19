package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}

object Main {
  def apply(name: String = "")(implicit context: ActorContext) = {
    new Reactor(name) {
      val props = Props[Main]
    }
  }
}

class Main extends FlowActor {
  val gen = Generate()
  val inc = Increment()
  val mul = Multiply()

  gen ~> inc ~> mul.input1 ; mul ~> Print()
  gen ~>        mul.input2
}
