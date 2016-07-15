package aspect.controllers.user

import akka.actor.Props
import aspect.common.Messages.{Done, Start}
import aspect.repositories.{RemoveSession, SessionRemoved, SessionRepository}
import aspect.rest.Controller

object LogoutController {
  def props(token: String) = Props(classOf[LogoutController], token)
}

class LogoutController(token: String) extends Controller {
  def receive = {
    case Start => SessionRepository.endpoint ! RemoveSession(token)
    case SessionRemoved(`token`) => complete(Done)
  }
}
