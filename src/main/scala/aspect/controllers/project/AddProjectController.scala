package aspect.controllers.project

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.common._
import aspect.domain.{UserId, ProjectId, Project}
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.Unauthorized

case class AddProjectData(name: String)
case class AddProjectResult(projectId: ProjectId)

object AddProjectController {
  def props(userId: UserId, data: AddProjectData) =
    Props(classOf[AddProjectController], userId.underlying, data)
}

class AddProjectController(userId: UserId, data: AddProjectData) extends Controller {
  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
    case UserFoundById(receivedUser) =>
      ProjectRepository.endpoint ! AddProject(Project(ProjectId.generate(), userId, data.name))
    case UserNotFoundById(`userId`) =>
      failure(Unauthorized.credentialsRejected)
    case ProjectAdded(projectId) =>
      complete(AddProjectResult(projectId))
  }
}
