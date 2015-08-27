package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{TargetId, KeywordId, Keyword}
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

object KeywordCollection extends MongoCollection[KeywordId, Keyword] {
  val name = "keywords"

  import TargetCollection.{TargetIdReader, TargetIdWriter}

  implicit object KeywordIdReader extends BSONReader[BSONString, KeywordId] {
    def read(bson: BSONString): KeywordId = KeywordId(bson.value)
  }

  implicit object KeywordIdWriter extends BSONWriter[KeywordId, BSONString] {
    def write(value: KeywordId): BSONString = BSONString(value.underlying)
  }

  implicit object KeywordReader extends BSONDocumentReader[Keyword] {
    def read(doc: BSONDocument) = Keyword(
      id = doc.getAs[KeywordId]("_id").get,
      value = doc.getAs[String]("value").get,
      targets = doc.getAs[Set[TargetId]]("targets").getOrElse(Set.empty))
  }

  implicit object KeywordWriter extends BSONDocumentWriter[Keyword] {
    def write(value: Keyword) = $doc(
      "_id" -> value.id,
      "value" -> value.value,
      "targets" -> value.targets)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("value" -> IndexType.Ascending), unique = true),
      Index(Seq("targets" -> IndexType.Ascending))
    ) map items.indexesManager.ensure).map(_ => {})
}
