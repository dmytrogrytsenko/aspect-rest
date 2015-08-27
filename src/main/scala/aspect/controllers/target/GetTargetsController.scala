package aspect.controllers.target

import akka.actor.Props
import akka.event.LoggingReceive
import aspect.common.Messages.Start
import aspect.domain._
import aspect.repositories._
import aspect.rest.Controller
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

case class TargetItemResult(id: TargetId, name: String, keywords: String)

object TargetItemResult {
  def apply(target: Target): TargetItemResult =
    TargetItemResult(target.id, target.name, target.keywords)
}

case class TargetListResult(targets: List[TargetItemResult])

object GetTargetsController {
  def props(userId: UserId, projectId: ProjectId) =
    Props(classOf[GetTargetsController], userId.underlying, projectId.underlying)
}

class GetTargetsController(userId: UserId, projectId: ProjectId) extends Controller {

  var user: User = null
  var project: Project = null

  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserById(userId)
      ProjectRepository.endpoint ! FindProjectById(projectId)

    case UserFoundById(receivedUser) => user = receivedUser; checkCollected()
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)

    case ProjectFoundById(receivedProject) => project = receivedProject; checkCollected()
    case ProjectNotFoundById(`projectId`) => failure(NotFound.projectNotFound)

    case ProjectTargets(`projectId`, targets) => complete(TargetListResult(targets.map(TargetItemResult.apply)))
  }

  def checkCollected() =
    if (user != null && project != null) {
      if (project.userId != user.id) failure(Forbidden.accessDenied)
      else TargetRepository.endpoint ! GetProjectTargets(projectId)
    }
}
