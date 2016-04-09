package aspect.experimental.flowing

import akka.actor.ActorRef

trait Reactor {
  def name: String
  def underlying: ActorRef
}
