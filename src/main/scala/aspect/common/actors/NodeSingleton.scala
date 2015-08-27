package aspect.common.actors

import akka.actor._
import aspect.common.Messages.Start

import scala.reflect.ClassTag

trait CustomNodeSingleton {
  protected var actor: ActorRef = null

  protected def createActor(props: Props, name: String)(implicit context: ActorContext) = {
    actor = context.actorOf(props, name)
    actor ! Start
    actor
  }

  protected def createTypedActor[TActor](args: Any*)(implicit actorTag: ClassTag[TActor], context: ActorContext) = {
    val props = Props(actorTag.runtimeClass, args: _*)
    val name = actorTag.runtimeClass.getSimpleName
    createActor(props, name)
  }

  def endpoint = actor
}

trait NodeSingleton[TActor] extends CustomNodeSingleton {
  def create(implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createTypedActor()
}

trait NodeSingleton1[TActor, TArg] extends CustomNodeSingleton {
  def create(arg: TArg)(implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createTypedActor(arg)
}

trait NodeSingleton2[TActor, TArg1, TArg2] extends CustomNodeSingleton {
  def create(arg1: TArg1, arg2: TArg2)(implicit actorTag: ClassTag[TActor], context: ActorContext) =
    createTypedActor(arg1, arg2)
}
