package aspect.experimental.flowing

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

  def apply[T](name: String)(implicit reactor: Reactor, context: ActorContext) = {
    val underlying = context.actorSelection(reactor.underlying.path / actorName(name))
    new Input[T](name, underlying)
  }

  def default[T](implicit reactor: Reactor, context: ActorContext) =
    apply[T](defaultName)
}
