package aspect.repositories

import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.mongo.MongoStorage
import aspect.mongo.KeywordCollection
import reactivemongo.api.DB

object KeywordRepository extends NodeSingleton[KeywordRepository]

class KeywordRepository extends BaseActor {

  import KeywordCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes
  }
}
