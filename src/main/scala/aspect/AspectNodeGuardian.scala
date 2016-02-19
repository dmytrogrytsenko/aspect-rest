package aspect

import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, HandlerGuardian}
import aspect.experimental.TwitterSearcher
import aspect.repositories._
import aspect.rest.RestGuardian

class AspectNodeGuardian extends BaseActor {

  def receive = {
    case Start =>
      HandlerGuardian.create
      UserRepository.create
      SessionRepository.create
      ProjectRepository.create
      TargetRepository.create
      KeywordRepository.create
      //Main()

      //if (cluster.selfRoles.contains("worker")) {
      //  KeywordsPreparer.create(Some("worker"))
      //}

      if (cluster.selfRoles.contains("rest")) {
        RestGuardian.create
        TwitterSearcher.create
      }
  }
}

