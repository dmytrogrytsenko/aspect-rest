package aspect.repositories

import aspect.common.Messages.Start
import aspect.common.actors.{NodeSingleton1, BaseActor}
import aspect.mongo.KeywordCollection
import reactivemongo.api.DB

object KeywordRepository extends NodeSingleton1[TargetRepository, DB]

class KeywordRepository(implicit val db: DB) extends BaseActor {

  import KeywordCollection._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes
  }
}
