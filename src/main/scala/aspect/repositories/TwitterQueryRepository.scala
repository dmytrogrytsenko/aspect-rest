package aspect.repositories

import aspect.common._
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.Messages.Start
import aspect.common.mongo.MongoStorage
import aspect.domain.TwitterQuery
import aspect.mongo.TwitterQueryCollection
import reactivemongo.api.DB

case object GetEnabledTwitterQueries
case class EnabledTwitterQueries(items: List[TwitterQuery])
case class UpdateTwitterQuery(request: TwitterQuery)

object TwitterQueryRepository extends NodeSingleton[TwitterQueryRepository]

class TwitterQueryRepository extends BaseActor {

  import TwitterQueryCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes
    case GetEnabledTwitterQueries => enabledQueries.map(EnabledTwitterQueries).pipeTo(sender())
    case UpdateTwitterQuery(request) => update(request.id, request)
  }
}