package aspect.controllers.project

import akka.actor.Props
import aspect.common.Messages.{Done, Start}
import aspect.domain.{UserId, ProjectId, Project, User}
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

case class UpdateProjectData(name: Option[String] = None)

object UpdateProjectController {
  def props(userId: UserId, projectId: ProjectId, data: UpdateProjectData) =
    Props(classOf[UpdateProjectController], userId.underlying, projectId.underlying, data)
}

class UpdateProjectController(userId: UserId, projectId: ProjectId, data: UpdateProjectData) extends Controller {

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
    case ProjectUpdated(`projectId`) => complete(Done)
  }

  def checkCollected() =
    if (user != null && project != null) {
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else ProjectRepository.endpoint ! UpdateProject(projectId, data.name)
    }
}
