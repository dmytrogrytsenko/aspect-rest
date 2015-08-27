package aspect.controllers.project

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.domain.{ProjectId, UserId, Project}
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.Unauthorized

case class ProjectItemResult(id: ProjectId, name: String)

object ProjectItemResult {
  def apply(project: Project): ProjectItemResult = ProjectItemResult(project.id, project.name)
}

case class ProjectListResult(projects: List[ProjectItemResult])

object GetProjectsController {
  def props(userId: UserId) = Props(classOf[GetProjectsController], userId.underlying)
}

class GetProjectsController(userId: UserId) extends Controller {
  def receive = {
    case Start => UserRepository.endpoint ! FindUserById(userId)
    case UserFoundById(user) => ProjectRepository.endpoint ! GetUserProjects(userId)
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)
    case UserProjects(`userId`, projects) => complete(ProjectListResult(projects.map(ProjectItemResult.apply)))
  }
}
