package aspect.experimental.flowing

import akka.actor.{Props, ActorContext}
import aspect.common._

abstract class Reactor(props: Props, name: String)(implicit context: ActorContext) {
  implicit val reactor = this
  val underlying = props.create(name)
}
