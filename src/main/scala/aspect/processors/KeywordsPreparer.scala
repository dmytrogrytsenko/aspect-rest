package aspect.processors

import akka.actor.ReceiveTimeout
import akka.actor.Status.Failure
import akka.util.Timeout
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, ClusterSingleton}
import aspect.common.mongo.MongoStorage
import aspect.domain._
import aspect.mongo.{KeywordCollection, TargetCollection, ProjectCollection, UserCollection}
import reactivemongo.api.DB

import scala.concurrent.duration._

object KeywordsPreparer extends ClusterSingleton[KeywordsPreparer]

class KeywordsPreparer extends BaseActor {

  case class Loaded(targets: Set[Target], keywords: Set[Keyword])

  import context.dispatcher

  import TargetCollection._
  import KeywordCollection._

  val successInterval = 1.minute
  val failureInterval = 1.minute
  val receiveTimeout = 2.minute

  implicit val db: DB = MongoStorage.connect.db("aspect")

  context.setReceiveTimeout(receiveTimeout)

  def receive = {
    case Start =>
      log.debug(s"Start")
      (for {
        targets <- TargetCollection.all
        keywords <- KeywordCollection.all
      } yield Loaded(targets.toSet, keywords.toSet)) pipeTo self

    case Loaded(targets, keywords) =>
      log.debug(s"Loaded")

      val rows = for {
        target <- targets
        word <- target.words
      } yield word -> target

      val newKeywords = rows
        .groupBy(_._1)
        .mapValues(row => Keyword(KeywordId(row.head._1), row.head._1, row.map(_._2.id).toSet))
        .values
        .toSet

      val keywordsToDelete = keywords.map(_.id) -- newKeywords.map(_.id)
      val keywordsToInsert = newKeywords.map(_.id) -- keywords.map(_.id)
      val keywordsToUpdate = (keywords.map(_.id) & newKeywords.map(_.id))
        .filterNot(id => keywords.find(_.id == id).get == newKeywords.find(_.id == id).get)

      log.debug("-Delete--------------------------------------")
      log.debug(keywordsToDelete.toString())
      log.debug("-Insert--------------------------------------")
      log.debug(keywordsToInsert.toString())
      log.debug("-Update--------------------------------------")
      log.debug(keywordsToUpdate.toString())
      log.debug("---------------------------------------")

      implicit val _ = Timeout(5.seconds)
      keywordsToDelete.foreach(id => KeywordCollection.remove(id).await)
      keywordsToInsert.foreach(id => KeywordCollection.add(newKeywords.find(_.id == id).get).await)
      keywordsToUpdate.foreach(id => KeywordCollection.update(id, newKeywords.find(_.id == id).get).await)

      log.debug(s"Completed. Sleep $successInterval")
      scheduleOnce(successInterval, Start)

    case Failure(e) =>
      log.error(e, s"KeywordPreparer failed. Sleep $failureInterval")
      scheduleOnce(failureInterval, Start)

    case ReceiveTimeout =>
      log.error(s"KeywordPreparer timed out. Sleep $failureInterval")
      scheduleOnce(failureInterval, Start)
  }
}
