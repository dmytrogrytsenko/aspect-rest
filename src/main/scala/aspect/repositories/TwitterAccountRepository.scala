package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{NodeSingleton1, BaseActor}
import aspect.domain.TwitterAccount
import aspect.mongo.TwitterAccountCollection
import reactivemongo.api.DB

case object GetTwitterAccounts
case class TwitterAccounts(accounts: List[TwitterAccount])

object TwitterAccountRepository extends NodeSingleton1[TwitterAccountRepository, DB]

class TwitterAccountRepository(implicit val db: DB) extends BaseActor {

  import TwitterAccountCollection._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes
    case GetTwitterAccounts => all.map(TwitterAccounts).pipeTo(sender())
  }
}
