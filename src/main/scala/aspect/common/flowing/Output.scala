package aspect.common.flowing

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

  def apply[T](reactorName: String, outputName: String)(implicit context: ActorContext) = {
    val underlying = context.actorSelection(context.self.path / reactorName / actorName(outputName))
    new Output[T](outputName, underlying)
  }

  def default[T](reactorName: String)(implicit context: ActorContext) =
    apply[T](reactorName, defaultName)
}
