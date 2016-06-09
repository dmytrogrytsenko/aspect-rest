package aspect.common.actors

import akka.actor.Stash
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberRemoved, MemberUp}
import akka.cluster.Member
import akka.routing.ConsistentHash
import aspect.common.Messages.Start
import aspect.common.config.Settings
import com.typesafe.config.Config

case class NodeRing(config: Config) extends BaseActor with Stash {
  val settings = NodeRingSettings(config)

  case class Node(member: Member) {
    override val toString = member.uniqueAddress.uid.toString
  }

  override def postStop() = {
    cluster.unsubscribe(self)
    super.postStop()
  }

  def receive = {
    case Start =>
      cluster.subscribe(self, classOf[MemberUp], classOf[MemberRemoved])
      become(waitingForClusterState)
  }

  def waitingForClusterState: Receive = {
    case state: CurrentClusterState =>
      val nodes = state.members.filter(hasRole).map(Node)
      val ring = ConsistentHash[Node](nodes, settings.virtualNodesFactor)
      become(working(ring))
      unstashAll()
    case _ => stash()
  }

  def working(ring: ConsistentHash[Node]): Receive ={
    case MemberUp(member) => if (hasRole(member)) become(working(ring :+ Node(member)))
    case MemberRemoved(member, _) => become(working(ring :- Node(member)))
  }

  def hasRole(member: Member) = settings.role.fold(true)(r => member.hasRole(r) || member.hasRole("all"))
}

case class NodeRingSettings(config: Config) extends Settings {
  val role = get[Option[String]]("role")
  val virtualNodesFactor = get[Int]("virtualNodesFactor")
}