package aspect.repositories

import aspect.common._
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.Messages.Start
import aspect.common.mongo.MongoStorage
import aspect.domain.TwitterRequest
import aspect.mongo.TwitterRequestCollection
import reactivemongo.api.DB

case object GetTwitterRequests
case class TwitterRequests(items: List[TwitterRequest])

object TwitterRequestRepository extends NodeSingleton[TwitterRequestRepository]

class TwitterRequestRepository extends BaseActor {

  import TwitterRequestCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes
    case GetTwitterRequests => all.map(TwitterRequests).pipeTo(sender())
  }
}