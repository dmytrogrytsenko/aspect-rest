package aspect.controllers.user

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.domain.{UserId, User}
import aspect.repositories.{FindUserById, UserFoundById, UserNotFoundById, UserRepository}
import aspect.rest.Controller
import aspect.rest.Errors.Unauthorized

case class ProfileResult(id: UserId,
                         name: String,
                         email: String,
                         firstName: Option[String],
                         lastName: Option[String],
                         company: Option[String])

object ProfileResult {
  def apply(user: User): ProfileResult = ProfileResult(
    id = user.id,
    name = user.name,
    email = user.email,
    firstName = user.firstName,
    lastName = user.lastName,
    company = user.company)
}

object GetProfileController {
  def props(userId: UserId) = Props(classOf[GetProfileController], userId.underlying)
}

class GetProfileController(userId: UserId) extends Controller {
  def receive = {
    case Start => UserRepository.endpoint ! FindUserById(userId)
    case UserFoundById(user) => complete(ProfileResult(user))
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)
  }
}