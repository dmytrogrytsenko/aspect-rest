package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.mongo.MongoStorage
import aspect.domain.{ProjectId, TargetId, Target}
import aspect.mongo.TargetCollection
import reactivemongo.api.DB

case class GetProjectTargets(projectId: ProjectId)
case class ProjectTargets(projectId: ProjectId, targets: List[Target])

case class FindTargetById(targetId: TargetId)
case class TargetFoundById(target: Target)
case class TargetNotFoundById(targetId: TargetId)

case class AddTarget(target: Target)
case class TargetAdded(targetId: TargetId)

case class RemoveTarget(targetId: TargetId)
case class TargetRemoved(targetId: TargetId)

case class UpdateTarget(targetId: TargetId, name: Option[String], keywords: Option[String])
case class TargetUpdated(targetId: TargetId)

object TargetRepository extends NodeSingleton[TargetRepository]

class TargetRepository extends BaseActor {

  import TargetCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes

    case GetProjectTargets(projectId) =>
      getProjectTargets(projectId) map (ProjectTargets(projectId, _)) pipeTo sender()

    case FindTargetById(targetId) =>
      get(targetId) map {
        case Some(target) => TargetFoundById(target)
        case None => TargetNotFoundById(targetId)
      } pipeTo sender()

    case AddTarget(target) =>
      add(target) map (_ => TargetAdded(target.id)) pipeTo sender()

    case RemoveTarget(targetId) =>
      remove(targetId) map (_ => TargetRemoved(targetId)) pipeTo sender()

    case UpdateTarget(targetId, name, keywords) =>
      update(targetId, name, keywords) map (_ => TargetUpdated(targetId)) pipeTo sender()
  }
}
