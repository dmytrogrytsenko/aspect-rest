package aspect.experimental.flowing

import akka.actor.{Actor, ActorContext}
import aspect.common._
import aspect.common.actors.BaseActor

trait FlowReactor extends BaseActor {
  def receive = Actor.emptyBehavior

  implicit class RichOutput[T](source: Output[T]) {
    def ~>(destination: Input[T])(implicit context: ActorContext): Unit = {
      Link.props(source.underlying, destination.underlying).create
    }

    def ~>[D <: DefaultInput[T]](destination: D)(implicit context: ActorContext): D = {
      source ~> destination.input
      destination
    }
  }

  implicit class RichDefaultOutput[T](source: DefaultOutput[T]) {
    def ~>(destination: Input[T])(implicit context: ActorContext): Unit = {
      Link.props(source.output.underlying, destination.underlying).create
    }

    def ~>[D <: DefaultInput[T]](destination: D)(implicit context: ActorContext): D = {
      source ~> destination.input
      destination
    }
  }
}
