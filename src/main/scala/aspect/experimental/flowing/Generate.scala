package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}
import aspect.common.Messages.{Stop, Start}
import aspect.common.actors.BaseActor
import Messages._

object Generate {
  def apply(name: String = "")(implicit context: ActorContext) = {
    new Reactor(name) with DefaultOutput[Int] {
      val props = Props[Generate]
      val output = Output.default[Int]
    }
  }
}

class Generate extends BaseActor {
  val output = Endpoint.createDefaultOutput

  def receive = {
    case Start => generate(0)
  }

  def generate(value: Int) = {
    val id = CorrelationId.generate
    output ! Send(id, Message.create("value" -> value))
    become(processing(id, value))
  }

  def processing(id: CorrelationId, value: Int, stopping: Boolean = false): Receive = {
    case Acknowledge(`id`) =>
      if (stopping) stop() else generate(value + 1)
    case Failed(`id`, e) =>
      log.error(e, "Send failed.")
      if (stopping) stop() else generate(value + 1)
    case Stop =>
      become(processing(id, value, stopping = true))
  }
}

