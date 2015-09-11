package aspect.experimental.messaging

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingAdapter

trait Endpoint {
  def underlying: ActorRef

  def !(message: Any)
       (implicit sender: ActorRef = Actor.noSender, log: LoggingAdapter): Unit = {
    log.debug(s"${if (sender != Actor.noSender) sender.path.name else "Unknown sender"} send $message to ${underlying.path.name}")
    underlying.!(message)(sender)
  }
}
