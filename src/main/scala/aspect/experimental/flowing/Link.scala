package aspect.experimental.flowing

import akka.actor.{Props, ActorSelection}
import aspect.common.actors.BaseActor

object Link {
  def props(source: ActorSelection, destination: ActorSelection): Props =
    Props(classOf[Link], source, destination)
}

class Link(source: ActorSelection, destination: ActorSelection) extends BaseActor {
  def receive = ???
}
