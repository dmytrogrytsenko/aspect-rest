package aspect.controllers.project

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.domain.{UserId, ProjectId, Project, User}
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

case class ProjectUserResult(id: UserId, name: String)
case class ProjectResult(id: ProjectId, name: String, owner: ProjectUserResult)

object GetProjectController {
  def props(userId: UserId, projectId: ProjectId) =
    Props(classOf[GetProjectController], userId.underlying, projectId.underlying)
}

class GetProjectController(userId: UserId, projectId: ProjectId) extends Controller {

  var user: User = null
  var project: Project = null

  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
      ProjectRepository.endpoint ! FindProjectById(projectId)
    case UserFoundById(receivedUser) => user = receivedUser; checkCompleted()
    case ProjectFoundById(receivedProject) => project = receivedProject; checkCompleted()
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)
    case ProjectNotFoundById(`projectId`) => failure(NotFound.projectNotFound)
  }

  def checkCompleted() =
    if (user != null && project != null) {
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else complete(ProjectResult(project.id, project.name, ProjectUserResult(user.id, user.name)))
    }
}
