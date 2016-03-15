package aspect

import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.repositories._
import aspect.rest.RestService

class AspectGuardian extends BaseActor {

  def receive = {
    case Start =>
      UserRepository.create
      SessionRepository.create
      ProjectRepository.create
      TargetRepository.create
      KeywordRepository.create

      if (cluster.selfRoles.contains("rest")) {
        RestService.create
      }
  }
}

