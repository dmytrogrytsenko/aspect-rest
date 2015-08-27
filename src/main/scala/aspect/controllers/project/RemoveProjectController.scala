package aspect.controllers.project

import akka.actor.Props
import aspect.common.Messages.{Done, Start}
import aspect.domain.{UserId, ProjectId, Project, User}
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

object RemoveProjectController {
  def props(userId: UserId, projectId: ProjectId) =
    Props(classOf[RemoveProjectController], userId.underlying, projectId.underlying)
}

class RemoveProjectController(userId: UserId, projectId: ProjectId) extends Controller {

  var user: User = null
  var project: Project = null

  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
      ProjectRepository.endpoint ! FindProjectById(projectId)
    case UserFoundById(receivedUser) => user = receivedUser; checkCollected()
    case ProjectFoundById(receivedProject) => project = receivedProject; checkCollected()
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)
    case ProjectNotFoundById(`projectId`) => failure(NotFound.projectNotFound)
    case ProjectRemoved(`projectId`) => complete(Done)
  }

  def checkCollected() =
    if (user != null && project != null) {
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else ProjectRepository.endpoint ! RemoveProject(projectId)
    }
}
