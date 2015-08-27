package aspect.controllers.user

import akka.actor._
import aspect.common.Messages.Start
import aspect.common._
import aspect.domain.Session
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.Unauthorized
import org.joda.time.DateTime

case class LoginData(login: String, password: String)

case class LoginResult(token: String)

object LoginController {
  def props(data: LoginData) = Props(classOf[LoginController], data)
}

class LoginController(data: LoginData) extends Controller {
  def receive = {
    case Start => UserRepository.endpoint ! FindUserByName(data.login)
    case UserFoundByName(user) if user.password == data.password =>
      val session = Session(newUUID, user.id, DateTime.now, DateTime.now)
      SessionRepository.endpoint ! AddSession(session)
    case UserFoundByName(user) => failure(Unauthorized.credentialsRejected)
    case UserNotFoundByName(userName) => failure(Unauthorized.credentialsRejected)
    case SessionAdded(token) => complete(LoginResult(token))
  }
}
