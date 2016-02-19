package aspect.experimental.flowing

import akka.actor.{ActorContext, ActorSelection}

trait DefaultOutput[T] {
  def output: Output[T]
}

case class Output[T](name: String, underlying: ActorSelection) extends DefaultOutput[T] {
  val output = this
}

object Output {
  val defaultName = "default"

  def actorName(name: String) = s"outputs.$name"

  def apply[T](name: String)(implicit reactor: Reactor, context: ActorContext) = {
    val underlying = context.actorSelection(reactor.underlying.path / actorName(name))
    new Output[T](name, underlying)
  }

  def default[T](implicit reactor: Reactor, context: ActorContext) =
    apply[T](defaultName)
}
