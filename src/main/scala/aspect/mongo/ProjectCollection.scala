package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{UserId, ProjectId, Project}
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

object ProjectCollection extends MongoCollection[ProjectId, Project] {

  val name = "projects"

  import UserCollection.{UserIdReader, UserIdWriter}

  implicit object ProjectIdReader extends BSONReader[BSONString, ProjectId] {
    def read(bson: BSONString): ProjectId = ProjectId(bson.value)
  }

  implicit object ProjectIdWriter extends BSONWriter[ProjectId, BSONString] {
    def write(value: ProjectId): BSONString = BSONString(value.underlying)
  }

  implicit object ProjectWriter extends BSONDocumentWriter[Project] {
    def write(value: Project) = $doc(
      "_id" -> value.id,
      "userId" -> value.userId,
      "name" -> value.name)
  }

  implicit object ProjectReader extends BSONDocumentReader[Project] {
    def read(doc: BSONDocument) = Project(
      id = doc.getAs[ProjectId]("_id").get,
      userId = doc.getAs[UserId]("userId").get,
      name = doc.getAs[String]("name").get)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("userId" -> IndexType.Ascending))
    ) map items.indexesManager.ensure).map(_ => {})

  def getUserProjects(userId: UserId)(implicit db: DB, ec: ExecutionContext): Future[List[Project]] =
    items.find($doc("userId" -> userId)).cursor[Project].collect[List]()

  def update(projectId: ProjectId, name: Option[String])(implicit db: DB, ec: ExecutionContext): Future[Unit] =
    if (name.isEmpty) Future.successful()
    else items.update($id(projectId), $set("name" -> name)).map(_ => { })
}
