package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{AccountHost, Account}
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, BSONDocument}

object AccountCollection extends MongoCollection[String, Account] {
  val name = "accounts"

  implicit object AccountHostReader extends BSONDocumentReader[AccountHost] {
    def read(doc: BSONDocument) = AccountHost(id = doc.getAs[String]("_id").get)
  }

  implicit object AccountHostWriter extends BSONDocumentWriter[AccountHost] {
    def write(value: AccountHost) = $doc("_id" -> value.id)
  }

  implicit object AccountReader extends BSONDocumentReader[Account] {
    def read(doc: BSONDocument) = Account(
      id = doc.getAs[String]("_id").get,
      url = doc.getAs[String]("url").get,
      host = doc.getAs[AccountHost]("host").get,
      name = doc.getAs[String]("name").get)
  }

  implicit object AccountWriter extends BSONDocumentWriter[Account] {
    def write(value: Account) = $doc(
      "_id" -> value.id,
      "url" -> value.url,
      "host" -> value.host,
      "name" -> value.name)
  }
}
