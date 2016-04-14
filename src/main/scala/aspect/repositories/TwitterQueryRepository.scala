package aspect.repositories

import aspect.common._
import aspect.common.actors.{NodeSingleton1, BaseActor}
import aspect.common.Messages.Start
import aspect.domain.twitter.TwitterQuery
import aspect.mongo.TwitterQueryCollection
import reactivemongo.api.DB

case object GetEnabledTwitterQueries
case class EnabledTwitterQueries(items: List[TwitterQuery])
case class UpdateTwitterQuery(query: TwitterQuery)

object TwitterQueryRepository extends NodeSingleton1[TwitterQueryRepository, DB]

class TwitterQueryRepository(implicit val db: DB) extends BaseActor {

  import TwitterQueryCollection._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes
    case GetEnabledTwitterQueries => enabledQueries.map(EnabledTwitterQueries).pipeTo(sender())
    case UpdateTwitterQuery(query) => update(query.id, query)
  }
}