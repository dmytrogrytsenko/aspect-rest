package aspect.processors

import akka.actor.{Stash, ActorContext, ActorRef}
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{ClusterSingleton, BaseActor}
import aspect.common.flowing.Messages.{CorrelationId, Request}
import aspect.common.flowing.{Output, Reactor, DefaultOutput}
import aspect.domain.{TwitterAccountId, TwitterAccount}
import aspect.gateways.twitter.client.RateLimitStatus
import aspect.repositories.{TwitterAccounts, GetTwitterAccounts, TwitterAccountRepository}
import org.joda.time.DateTime

import scala.collection.mutable

case class TwitterAccountSource(name: String, underlying: ActorRef, output: Output[TwitterAccount])
  extends Reactor with DefaultOutput[TwitterAccount]

object TwitterAccountSource {
  def create(implicit context: ActorContext) = {
    val name = this.getClass.getSimpleName
    val underlying = TwitterAccountSourceReactor.endpoint
    val output = Output[TwitterAccount](Output.defaultName, context.actorSelection(underlying.path))
    TwitterAccountSource(name, underlying, output)
  }
}

object TwitterAccountSourceReactor extends ClusterSingleton[TwitterAccountSourceReactor]

class TwitterAccountSourceReactor extends BaseActor with Stash {

  private var accounts: Map[TwitterAccountId, TwitterAccount] = Map.empty
  private val requests = mutable.Queue.empty[CorrelationId]
  private val aquiredAccounts: Map[TwitterAccountId, DateTime] = Map.empty
  private val statuses: Map[TwitterAccountId, RateLimitStatus] = Map.empty

  def receive = {
    case Start => TwitterAccountRepository.endpoint !! GetTwitterAccounts
    case TwitterAccounts(items) =>
      accounts = items.map(item => item.id -> item).toMap
      become(producing)
      unstashAll()
    case _ => stash()
  }

  def producing: Receive = {
    case Request(reqId) =>
      requests.enqueue()
  }
}