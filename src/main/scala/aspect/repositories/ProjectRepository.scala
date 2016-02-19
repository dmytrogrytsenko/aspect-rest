package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.mongo.MongoStorage
import aspect.domain.{ProjectId, UserId, Project}
import aspect.mongo.ProjectCollection
import reactivemongo.api.DB

case class GetUserProjects(userId: UserId)
case class UserProjects(userId: UserId, projects: List[Project])

case class FindProjectById(projectId: ProjectId)
case class ProjectFoundById(project: Project)
case class ProjectNotFoundById(projectId: ProjectId)

case class AddProject(project: Project)
case class ProjectAdded(projectId: ProjectId)

case class RemoveProject(projectId: ProjectId)
case class ProjectRemoved(projectId: ProjectId)

case class UpdateProject(projectId: ProjectId, name: Option[String])
case class ProjectUpdated(projectId: ProjectId)

object ProjectRepository extends NodeSingleton[ProjectRepository]

class ProjectRepository extends BaseActor {

  import ProjectCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes

    case GetUserProjects(userId) =>
      getUserProjects(userId) map (UserProjects(userId, _)) pipeTo sender()

    case FindProjectById(projectId) =>
      get(projectId) map {
        case Some(project) => ProjectFoundById(project)
        case None => ProjectNotFoundById(projectId)
      } pipeTo sender()

    case AddProject(project) =>
      add(project) map (_ => ProjectAdded(project.id)) pipeTo sender()

    case RemoveProject(projectId) =>
      remove(projectId) map (_ => ProjectRemoved(projectId)) pipeTo sender()

    case UpdateProject(projectId, projectName) =>
      update(projectId, projectName) map (_ => ProjectUpdated(projectId)) pipeTo sender()
  }
}
