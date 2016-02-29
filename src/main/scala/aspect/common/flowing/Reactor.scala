package aspect.common.flowing

import akka.actor.ActorRef

trait Reactor {
  def name: String
  def underlying: ActorRef
}
