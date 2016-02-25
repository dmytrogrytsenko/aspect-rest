package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.experimental.flowing.Messages.{Accepted, Message, Send, CorrelationId}

object Generate {
  def apply(name: String = "")(implicit context: ActorContext) =
    new Reactor(Props[Generate], name) with DefaultOutput[Int] {
      val output = Output.default[Int]
    }
}

class Generate extends BaseActor {
  val output = Endpoint.createDefaultOutput

  def receive = {
    case Start => generate(0)
  }

  def generate(value: Int) = {
    val id = CorrelationId.generate
    output !! Send(id, Message.create("value" -> value))
    become(sending(id, value))
  }

  def sending(id: CorrelationId, value: Int): Receive = {
    case Accepted(`id`) => generate(value + 1)
  }
}

