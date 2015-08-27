package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{UserId, User}
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.api.DB
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

object UserCollection extends MongoCollection[UserId, User] {

  val name = "users"

   implicit object UserIdReader extends BSONReader[BSONString, UserId] {
     def read(bson: BSONString): UserId = UserId(bson.value)
   }

   implicit object UserIdWriter extends BSONWriter[UserId, BSONString] {
     def write(value: UserId): BSONString = BSONString(value.underlying)
   }

   implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(value: User) = $doc(
      "_id" -> value.id,
      "name" -> value.name,
      "nameLC" -> value.name.toLowerCase,
      "password" -> value.password,
      "email" -> value.email,
      "firstName" -> value.firstName,
      "lastName" -> value.lastName,
      "company" -> value.company)
  }

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument) = User(
      id = doc.getAs[UserId]("_id").get,
      name = doc.getAs[String]("name").get,
      password = doc.getAs[String]("password").get,
      email = doc.getAs[String]("email").get,
      firstName = doc.getAs[String]("firstName"),
      lastName = doc.getAs[String]("lastName"),
      company = doc.getAs[String]("company"))
  }

   override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
     Future.sequence(List(
       Index(Seq("nameLC" -> IndexType.Ascending), unique = true)
     ) map items.indexesManager.ensure).map(_ => {})

   def findUserByName(username: String)(implicit db: DB, ec: ExecutionContext): Future[Option[User]] =
    items.find($doc("nameLC" -> username.toLowerCase)).one[User]
}
