package aspect.experimental.messaging

import akka.actor._
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.experimental.messaging.Messages._

trait Link[T] extends Endpoint

object Link {
  val defaultName = "default"
}

case class Output[T](underlying: ActorRef) extends Link[T]

object Output {
  def create[T](reactorName: String, outputName: String = Link.defaultName)
               (implicit context: ActorContext) = {
    val name = s"$reactorName.outputs.$outputName"
    val underlying = LinkActor.props.create(name)
    Output[T](underlying)
  }
}

case class Input[T](underlying: ActorRef) extends Link[T]

object Input {
  def create[T](reactorName: String, outputName: String = Link.defaultName)
               (implicit context: ActorContext) = {
    val name = s"$reactorName.inputs.$outputName"
    val underlying = LinkActor.props.create(name)
    Input[T](underlying)
  }
}

object LinkActor {
  def props = Props[LinkActor]
}

class LinkActor extends BaseActor with Stash {

  def receive = {
    case Start =>
      unstashAll()
      become(linking(None, None))
    case _ =>
      stash()
  }

  def linking(inputOpt: Option[Endpoint], outputOpt: Option[Endpoint]): Receive = {
    case LinkInput(endpoint) =>
      link(Some(endpoint), outputOpt)
      sender() ! InputLinked(endpoint)
    case LinkOutput(endpoint) =>
      link(inputOpt, Some(endpoint))
      sender() ! OutputLinked(endpoint)
    case _ =>
      stash()
  }

  def link(inputOpt: Option[Endpoint], outputOpt: Option[Endpoint]) = {
    become(linking(inputOpt, outputOpt))
    for {
      input <- inputOpt
      output <- outputOpt
    } {
      unstashAll()
      become(processing(input, output))
    }
  }

  def processing(input: Endpoint, output: Endpoint): Receive = {
    case msg if sender() == input.underlying =>
      output ! msg
    case msg if sender() == output.underlying =>
      input ! msg
    case _ =>
      log.error(s"Incorrect sender ${sender().path.toString}.")
  }
}
