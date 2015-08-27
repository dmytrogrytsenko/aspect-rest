package aspect.controllers.target

import akka.actor.Props
import aspect.common._
import aspect.common.Messages.Start
import aspect.domain._
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

case class AddTargetData(projectId: ProjectId, name: String, keywords: String)
case class AddTargetResult(targetId: TargetId)

object AddTargetController {
  def props(userId: UserId, data: AddTargetData) =
    Props(classOf[AddTargetController], userId.underlying, data)
}

class AddTargetController(userId: UserId, data: AddTargetData) extends Controller {
  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
      become(waitingForUser)
  }

  def waitingForUser: Receive = {
    case UserFoundById(user) =>
      ProjectRepository.endpoint ! FindProjectById(data.projectId)
      become(waitingForProject(user))
    case UserNotFoundById(`userId`) =>
      failure(Unauthorized.credentialsRejected)
  }

  def waitingForProject(user: User): Receive = {
    case ProjectFoundById(project) if project.id == data.projectId =>
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else {
        val target = Target(TargetId.generate(), data.projectId, data.name, data.keywords)
        TargetRepository.endpoint ! AddTarget(target)
        become(waitingForTargetAdded(user, project, target))
      }
    case ProjectNotFoundById(projectId) if projectId == data.projectId =>
      failure(NotFound.projectNotFound)
  }

  def waitingForTargetAdded(user: User, project: Project, target: Target): Receive = {
    case TargetAdded(targetId) if targetId == target.id =>
      complete(AddTargetResult(targetId))
  }
}
