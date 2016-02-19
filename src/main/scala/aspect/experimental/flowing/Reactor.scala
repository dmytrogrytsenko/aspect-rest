package aspect.experimental.flowing

import akka.actor.{ActorRef, Props, ActorContext}
import aspect.common._

abstract class Reactor(reactorName: String = "")(implicit context: ActorContext) {
  implicit val reactor = this
  def props: Props
  val name: String = if (reactorName == "") this.getClass.getSimpleName else reactorName
  val underlying: ActorRef = props.create(name)
}
