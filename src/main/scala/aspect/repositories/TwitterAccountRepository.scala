package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.mongo.MongoStorage
import aspect.domain.TwitterAccount
import aspect.mongo.TwitterAccountCollection
import reactivemongo.api.DB

case object GetTwitterAccounts
case class TwitterAccounts(accounts: List[TwitterAccount])

object TwitterAccountRepository extends NodeSingleton[TwitterAccountRepository]

class TwitterAccountRepository extends BaseActor {

  import TwitterAccountCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes
    case GetTwitterAccounts => all.map(TwitterAccounts).pipeTo(sender())
  }
}
