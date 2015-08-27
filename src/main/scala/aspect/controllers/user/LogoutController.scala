package aspect.controllers.user

import akka.actor.Props
import aspect.common.Messages.{Done, Start}
import aspect.common.actors.SingleUseActor
import aspect.repositories.{RemoveSession, SessionRemoved, SessionRepository}

object LogoutController {
  def props(token: String) = Props(classOf[LogoutController], token)
}

class LogoutController(token: String) extends SingleUseActor {
  def receive = {
    case Start => SessionRepository.endpoint ! RemoveSession(token)
    case SessionRemoved(`token`) => complete(Done)
  }
}
