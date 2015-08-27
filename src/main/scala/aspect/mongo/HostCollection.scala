package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.Host
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, BSONDocument}

object HostCollection extends MongoCollection[String, Host] {
  val name = "hosts"

  implicit object HostReader extends BSONDocumentReader[Host] {
    def read(doc: BSONDocument) = Host(
      id = doc.getAs[String]("_id").get,
      url = doc.getAs[String]("url").get)
  }

  implicit object HostWriter extends BSONDocumentWriter[Host] {
    def write(value: Host) = $doc(
      "_id" -> value.id,
      "url" -> value.url)
  }
}
