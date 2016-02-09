package aspect.experimental.messaging

import akka.actor.{ActorContext, ActorRef, Props}
import akka.event.LoggingAdapter
import aspect.common._
import aspect.common.Messages.Start
import aspect.experimental.messaging.Messages._

case class Generator(underlying: ActorRef, output: Output[Int]) extends Reactor

object Generator {
  def create(name: String)(implicit context: ActorContext, log: LoggingAdapter): Generator = {
    val output = Output.create[Int](name)
    val underlying = GeneratorActor.props(output).create(name)
    val generator = Generator(underlying, output)
    output ! LinkInput(generator)
    generator
  }
}

object GeneratorActor {
  def props(output: Output[Int]) = Props(classOf[GeneratorActor], output)
}

class GeneratorActor(output: Output[Int]) extends ReactorActor {
  def receive = {
    case Start =>
      generate(0)
      sender() ! Generator(self, output)
  }

  def generate(value: Int) = {
    val id = CorrelationId.generate
    output ! Send(id, Message.create("value" -> value))
    become(waitingForAccepted(id, value))
  }

  def waitingForAccepted(id: CorrelationId, value: Int): Receive = {
    case Accepted(`id`) =>
      generate(value + 1)
    case Failed(`id`, e) =>
      log.error(e, "Sending failed.")
      generate(value + 1)
  }
}
