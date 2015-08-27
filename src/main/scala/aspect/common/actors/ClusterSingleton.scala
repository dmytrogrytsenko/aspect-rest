package aspect.common.actors

import akka.actor._
import akka.cluster.ClusterEvent._
import aspect.common.Messages.Start
import aspect.common._

import scala.reflect.ClassTag

trait CustomClusterSingleton[TActor] extends CustomNodeSingleton {
  protected def createManager(role: Option[String], props: Props)
                             (implicit context: ActorContext) =
    createTypedActor[ClusterSingletonManager](role, props)

  protected def createTypedManager(role: Option[String], args: Any*)
                                  (implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createManager(role, Props(actorTag.runtimeClass, args: _*))
}

trait ClusterSingleton[TActor] extends CustomClusterSingleton[TActor] {
  def create(role: Option[String])
            (implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createTypedManager(role)
}

trait ClusterSingleton1[TActor, TArg] extends CustomClusterSingleton[TActor] {
  def create(role: Option[String], arg: TArg)
            (implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createTypedManager(role, arg)
}

trait ClusterSingleton2[TActor, TArg1, TArg2] extends CustomClusterSingleton[TActor] {
  def create(role: Option[String], arg1: TArg1, arg2: TArg2)
            (implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createTypedManager(role, arg1, arg2)
}

class ClusterSingletonManager(role: Option[String], props: Props) extends BaseActor with Stash {

  override def preStart() =
    cluster.subscribe(self, InitialStateAsEvents, classOf[LeaderChanged], classOf[RoleLeaderChanged])

  override def postStop() =
    cluster.unsubscribe(self)

  override def aroundReceive(body: Receive, msg: Any) = msg match {
    case Start =>
    case LeaderChanged(_) => if (role.isEmpty) changeLeader()
    case RoleLeaderChanged(receivedRole, _) => if (role.contains(receivedRole)) changeLeader()
    case _ => super.aroundReceive(body, msg)
  }

  def receive = waitingForLeader

  def waitingForLeader: Receive = {
    case _ => stash()
  }

  def leader(destination: ActorRef): Receive = {
    case msg => destination forward msg
  }

  def router(destination: ActorSelection): Receive = {
    case msg => destination forward msg
  }

  def changeLeader() = {
    context.children.foreach(_ ! PoisonPill)
    leaderNode match {
      case None => become(waitingForLeader)
      case Some(address) if address == cluster.selfAddress => become(leader(props.create(newUUID)))
      case Some(address) => become(router(self.on(address)))
    }
    unstashAll()
  }

  def leaderNode = role.fold(cluster.state.leader)(cluster.state.roleLeader)
}
