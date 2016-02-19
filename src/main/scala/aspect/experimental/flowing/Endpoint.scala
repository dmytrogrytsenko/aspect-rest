package aspect.experimental.flowing

import akka.actor.{ActorContext, Props}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor

object Endpoint {
  def props = Props[Endpoint]
  def create(actorName: String)(implicit context: ActorContext) = Endpoint.props.create(actorName)
  def createInput(name: String)(implicit context: ActorContext) = create(s"inputs.$name")
  def createOutput(name: String)(implicit context: ActorContext) = create(s"outputs.$name")
  def createDefaultInput(implicit context: ActorContext) = createInput("default")
  def createDefaultOutput(implicit context: ActorContext) = createOutput("default")
}

class Endpoint extends BaseActor {
  def receive = {
    case Start =>
  }
}

