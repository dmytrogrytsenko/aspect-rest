package aspect.common.actors

import akka.actor.Stash
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberRemoved, MemberUp}
import akka.cluster.Member
import akka.routing.ConsistentHash
import aspect.common.Messages.Start
import aspect.common.actors.NodeRings.GetShards
import aspect.common.config.Settings
import com.typesafe.config.Config

object NodeRings {
  case object GetShards
}

case class NodeRings(config: Config) extends BaseActor with Stash {
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
      become(working(Map.empty))
      unstashAll()
    case _ => stash()
  }

  def working(rings: Map[String, ConsistentHash[Node]]): Receive ={
    case MemberUp(member) =>
      val updatedRings = rings.map { case (role, ring) =>
        role -> (if (member.roles.contains(role)) ring :+ Node(member) else ring)
      }
      become(working(updatedRings))
    case MemberRemoved(member, _) =>
      become(working(rings.mapValues(_ :- Node(member))))
    case GetShards =>
    //val nodes = state.members.filter(hasRole).map(Node)
    //val ring = ConsistentHash[Node](nodes, settings.virtualNodesFactor)
  }

}

case class NodeRingSettings(config: Config) extends Settings {
  val virtualNodesFactor = get[Option[Int]]("virtualNodesFactor")
}

case class NodeRingsSettings(config: Config) extends Settings {
  val DefaultVirtualNodeFactor: Int = 1024
  val rings = get[Option[Map[String, NodeRingSettings]]]("rings")
  val virtualNodesFactor = get[Option[Int]]("virtualNodesFactor")
  def factor(role: String): Int = rings
    .flatMap(_.get(role))
    .flatMap(_.virtualNodesFactor)
    .orElse(virtualNodesFactor)
    .getOrElse(DefaultVirtualNodeFactor)
}
