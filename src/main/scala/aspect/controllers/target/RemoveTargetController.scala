package aspect.controllers.target

import akka.actor.Props
import aspect.common.Messages.{Done, Start}
import aspect.domain.{UserId, TargetId, Target, User}
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

object RemoveTargetController {
  def props(userId: UserId, targetId: TargetId) =
    Props(classOf[RemoveTargetController], userId.underlying, targetId.underlying)
}

class RemoveTargetController(userId: UserId, targetId: TargetId) extends Controller {
  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
      become(waitingForUser)
  }

  def waitingForUser: Receive = {
    case UserFoundById(user) if user.id == userId =>
      TargetRepository.endpoint ! FindTargetById(targetId)
      become(waitingForTarget(user))
    case UserNotFoundById(`userId`) =>
      failure(Unauthorized.credentialsRejected)
  }

  def waitingForTarget(user: User): Receive = {
    case TargetFoundById(target) if target.id == targetId =>
      ProjectRepository.endpoint ! FindProjectById(target.projectId)
      become(waitingForProject(user, target))
    case TargetNotFoundById(`targetId`) =>
      failure(NotFound.targetNotFound)
  }

  def waitingForProject(user: User, target: Target): Receive = {
    case ProjectFoundById(project) if project.id == target.projectId =>
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else {
        TargetRepository.endpoint ! RemoveTarget(targetId)
        become(waitingForTargetRemoved)
      }
    case ProjectNotFoundById(projectId) if projectId == target.projectId =>
      failure(NotFound.projectNotFound)
  }

  def waitingForTargetRemoved: Receive = {
    case TargetRemoved(`targetId`) => complete(Done)
  }
}
