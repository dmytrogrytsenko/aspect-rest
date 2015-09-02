package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{AccountId, HostId, AccountHost, Account}
import reactivemongo.bson._

object AccountCollection extends MongoCollection[AccountId, Account] {
  val name = "accounts"

  import HostCollection.HostIdReader
  import HostCollection.HostIdWriter

  implicit object AccountIdReader extends BSONReader[BSONString, AccountId] {
    def read(bson: BSONString): AccountId = AccountId(bson.value)
  }

  implicit object AccountIdWriter extends BSONWriter[AccountId, BSONString] {
    def write(value: AccountId): BSONString = BSONString(value.underlying)
  }

  implicit object AccountHostReader extends BSONDocumentReader[AccountHost] {
    def read(doc: BSONDocument) = AccountHost(id = doc.getAs[HostId]("_id").get)
  }

  implicit object AccountHostWriter extends BSONDocumentWriter[AccountHost] {
    def write(value: AccountHost) = $doc("_id" -> value.id)
  }

  implicit object AccountReader extends BSONDocumentReader[Account] {
    def read(doc: BSONDocument) = Account(
      id = doc.getAs[AccountId]("_id").get,
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
