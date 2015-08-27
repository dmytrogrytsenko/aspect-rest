package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{ProjectId, TargetId, Target}
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

object TargetCollection extends MongoCollection[TargetId, Target] {

  val name = "targets"

  import ProjectCollection.{ProjectIdReader, ProjectIdWriter}

  implicit object TargetIdReader extends BSONReader[BSONString, TargetId] {
    def read(bson: BSONString): TargetId = TargetId(bson.value)
  }

  implicit object TargetIdWriter extends BSONWriter[TargetId, BSONString] {
    def write(value: TargetId): BSONString = BSONString(value.underlying)
  }

  implicit object TargetWriter extends BSONDocumentWriter[Target] {
    def write(value: Target) = $doc(
      "_id" -> value.id,
      "projectId" -> value.projectId,
      "name" -> value.name,
      "keywords" -> value.keywords)
  }

  implicit object TargetReader extends BSONDocumentReader[Target] {
    def read(doc: BSONDocument) = Target(
      id = doc.getAs[TargetId]("_id").get,
      projectId = doc.getAs[ProjectId]("projectId").get,
      name = doc.getAs[String]("name").get,
      keywords = doc.getAs[String]("keywords").get)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("projectId" -> IndexType.Ascending))
    ) map items.indexesManager.ensure).map(_ => {})

  def getProjectTargets(projectId: ProjectId)
                       (implicit db: DB, ec: ExecutionContext): Future[List[Target]] =
    items.find($doc("projectId" -> projectId)).cursor[Target].collect[List]()

  def update(targetId: TargetId, name: Option[String] = None, keywords: Option[String] = None)
            (implicit db: DB, ec: ExecutionContext): Future[Unit] =
    if (name.isEmpty && keywords.isEmpty) Future.successful()
    else items.update($id(targetId), $set("name" -> name, "keywords" -> keywords)).map(_ => {})
}
