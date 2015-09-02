package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{HostId, Host}
import reactivemongo.bson._

object HostCollection extends MongoCollection[HostId, Host] {
  val name = "hosts"

  implicit object HostIdReader extends BSONReader[BSONString, HostId] {
    def read(bson: BSONString): HostId = HostId(bson.value)
  }

  implicit object HostIdWriter extends BSONWriter[HostId, BSONString] {
    def write(value: HostId): BSONString = BSONString(value.underlying)
  }

  implicit object HostReader extends BSONDocumentReader[Host] {
    def read(doc: BSONDocument) = Host(
      id = doc.getAs[HostId]("_id").get,
      url = doc.getAs[String]("url").get)
  }

  implicit object HostWriter extends BSONDocumentWriter[Host] {
    def write(value: Host) = $doc(
      "_id" -> value.id,
      "url" -> value.url)
  }
}
