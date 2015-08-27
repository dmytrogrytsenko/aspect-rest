package aspect.common.actors

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.event.LoggingReceive
import aspect.common.Messages.Start

object HandlerGuardian extends NodeSingleton[HandlerGuardian]

class HandlerGuardian extends BaseActor {

  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1) {
    case _ => Stop
  }

  def receive = LoggingReceive {
    case Start =>
    case props: Props => context.actorOf(props) forward Start
  }

}
