package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{TwitterAccountId, TwitterAccount}
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._

import scala.concurrent.{Future, ExecutionContext}

object TwitterAccountCollection extends MongoCollection[TwitterAccountId, TwitterAccount] {

  val name = "twitterAccounts"

  implicit object TwitterAccountIdReader extends BSONReader[BSONString, TwitterAccountId] {
    def read(bson: BSONString): TwitterAccountId = TwitterAccountId(bson.value)
  }

  implicit object TwitterAccountIdWriter extends BSONWriter[TwitterAccountId, BSONString] {
    def write(value: TwitterAccountId): BSONString = BSONString(value.underlying)
  }

  implicit object TwitterAccountReader extends BSONDocumentReader[TwitterAccount] {
    def read(doc: BSONDocument) = TwitterAccount(
      id = doc.getAs[TwitterAccountId]("_id").get,
      email = doc.getAs[String]("email").get,
      emailPassword = doc.getAs[String]("emailPassword").get,
      twitterUserId = doc.getAs[String]("twitterUserId").get,
      twitterScreenName = doc.getAs[String]("twitterScreenName").get,
      twitterPassword = doc.getAs[String]("twitterPassword").get,
      consumerKey = doc.getAs[String]("consumerKey").get,
      consumerSecret = doc.getAs[String]("consumerSecret").get,
      accessToken = doc.getAs[String]("accessToken").get,
      accessTokenSecret = doc.getAs[String]("accessTokenSecret").get)
  }

  implicit object TwitterAccountWriter extends BSONDocumentWriter[TwitterAccount] {
    def write(value: TwitterAccount) = $doc(
      "_id" -> value.id,
      "email" -> value.email,
      "emailPassword" -> value.emailPassword,
      "twitterUserId" -> value.twitterUserId,
      "twitterScreenName" -> value.twitterScreenName,
      "twitterPassword" -> value.twitterPassword,
      "consumerKey" -> value.consumerKey,
      "consumerSecret" -> value.consumerSecret,
      "accessToken" -> value.accessToken,
      "accessTokenSecret" -> value.accessTokenSecret)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("email" -> IndexType.Ascending), unique = true),
      Index(Seq("twitterUserId" -> IndexType.Ascending), unique = true),
      Index(Seq("twitterScreenName" -> IndexType.Ascending), unique = true)
    ) map items.indexesManager.ensure).map(_ => {})
}
