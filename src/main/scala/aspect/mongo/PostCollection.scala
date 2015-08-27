package aspect.mongo

import aspect.common.mongo.MongoCollection
import aspect.domain.{PostAuthor, PostHost, Post}
import org.joda.time.DateTime
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, BSONDocument}
import reactivemongo.extensions.dao.Handlers._

object PostCollection extends MongoCollection[String, Post] {
  val name = "posts"

  implicit object PostHostReader extends BSONDocumentReader[PostHost] {
    def read(doc: BSONDocument) = PostHost(id = doc.getAs[String]("_id").get)
  }

  implicit object PostHostWriter extends BSONDocumentWriter[PostHost] {
    def write(value: PostHost) = $doc("_id" -> value.id)
  }

  implicit object PostAuthorReader extends BSONDocumentReader[PostAuthor] {
    def read(doc: BSONDocument) = PostAuthor(
      id = doc.getAs[String]("_id").get,
      url = doc.getAs[String]("url").get,
      name = doc.getAs[String]("name").get)
  }

  implicit object PostAuthorWriter extends BSONDocumentWriter[PostAuthor] {
    def write(value: PostAuthor) = $doc(
      "_id" -> value.id,
      "url" -> value.url,
      "name" -> value.name)
  }

  implicit object PostReader extends BSONDocumentReader[Post] {
    def read(doc: BSONDocument) = Post(
      id = doc.getAs[String]("_id").get,
      url = doc.getAs[String]("url").get,
      host = doc.getAs[PostHost]("host").get,
      author = doc.getAs[PostAuthor]("author").get,
      publishTime = doc.getAs[DateTime]("publishTime").get,
      lastUpdateTime = doc.getAs[DateTime]("lastUpdateTime"),
      title = doc.getAs[String]("title"),
      text = doc.getAs[String]("text"),
      keywords = doc.getAs[Set[String]]("keywords"))
  }

  implicit object PostWriter extends BSONDocumentWriter[Post] {
    def write(value: Post) = $doc(
      "_id" -> value.id,
      "url" -> value.url,
      "host" -> value.host,
      "author" -> value.author,
      "publishTime" -> value.publishTime,
      "lastUpdateTime" -> value.lastUpdateTime,
      "title" -> value.title,
      "text" -> value.text,
      "keywords" -> value.keywords)
  }

}
