package aspect.experimental.flowing

import akka.actor.{ActorRef, Props, ActorContext}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import Messages._

case class Generate(name: String, underlying: ActorRef, output: Output[Int])
  extends Reactor with DefaultOutput[Int]

object Generate {
  def create(implicit context: ActorContext): Generate = {
    val name = this.getClass.getSimpleName
    val underlying = Props[GenerateReactor].create(name)
    val output = Output.default[Int](name)
    Generate(name, underlying, output)
  }
}

class GenerateReactor extends BaseActor {
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

