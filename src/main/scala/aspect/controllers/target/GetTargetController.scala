package aspect.controllers.target

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.domain._
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

case class TargetUserResult(id: UserId, username: String)
case class TargetProjectResult(id: ProjectId, name: String, owner: TargetUserResult)
case class TargetResult(id: TargetId, name: String, keywords: String, project: TargetProjectResult)

object GetTargetController {
  def props(userId: UserId, targetId: TargetId) =
    Props(classOf[GetTargetController], userId.underlying, targetId.underlying)
}

class GetTargetController(userId: UserId, targetId: TargetId) extends Controller {

  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
      become(waitingForUser)
  }

  def waitingForUser: Receive = {
    case UserFoundById(user) =>
      TargetRepository.endpoint ! FindTargetById(targetId)
      become(waitingForTarget(user))
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)
  }

  def waitingForTarget(user: User): Receive = {
    case TargetFoundById(target) =>
      ProjectRepository.endpoint ! FindProjectById(target.projectId)
      become(waitingForProject(user, target))
    case TargetNotFoundById(`targetId`) => failure(NotFound.targetNotFound)
  }

  def waitingForProject(user: User, target: Target): Receive = {
    case ProjectFoundById(project) =>
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else complete(TargetResult(target.id, target.name, target.keywords,
        TargetProjectResult(project.id, project.name, TargetUserResult(user.id, user.name))))
    case ProjectNotFoundById(projectId) if projectId == target.projectId => failure(NotFound.projectNotFound)
  }
}
