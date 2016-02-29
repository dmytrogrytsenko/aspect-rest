package aspect.common.flowing

import akka.actor.{ActorContext, ActorSelection}

trait DefaultInput[T] {
  def input: Input[T]
}

case class Input[T](name: String, underlying: ActorSelection) extends DefaultInput[T] {
  val input = this
}

object Input {
  val defaultName = "default"

  def actorName(name: String) = s"inputs.$name"

  def apply[T](reactionName: String, inputName: String)(implicit context: ActorContext) = {
    val underlying = context.actorSelection(context.self.path / reactionName / actorName(inputName))
    new Input[T](inputName, underlying)
  }

  def default[T](reactionName: String)(implicit context: ActorContext) =
    apply[T](reactionName, defaultName)
}
