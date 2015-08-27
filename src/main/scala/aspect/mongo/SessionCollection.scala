package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{UserId, Session}
import org.joda.time.DateTime
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import reactivemongo.extensions.dao.Handlers._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object SessionCollection extends MongoCollection[String, Session] {

  val name = "sessions"

  import UserCollection.{UserIdReader, UserIdWriter}

  implicit object SessionWriter extends BSONDocumentWriter[Session] {
    def write(value: Session) = $doc(
      "_id" -> value.token,
      "userId" -> value.userId,
      "createdAt" -> value.createdAt,
      "lastActivityAt" -> value.lastActivityAt)
  }

  implicit object SessionReader extends BSONDocumentReader[Session] {
    def read(doc: BSONDocument) = Session(
      token = doc.getAs[String]("_id").get,
      userId = doc.getAs[UserId]("userId").get,
      createdAt = doc.getAs[DateTime]("createdAt").get,
      lastActivityAt = doc.getAs[DateTime]("lastActivityAt").get)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("lastActivityAt" -> IndexType.Descending), options = $doc("expireAfterSeconds" -> 14.days.toSeconds))
    ) map items.indexesManager.ensure).map(_ => {})

  def activity(token: String)(implicit db: DB, ec: ExecutionContext): Future[Unit] =
    items.update($id(token), $set("lastActivityAt" -> DateTime.now)).map(_ => { }).recover { case e => println(e) }
}
