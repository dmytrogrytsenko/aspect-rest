package aspect.common.flowing

import akka.actor.Props
import aspect.common.actors.BaseActor
import com.typesafe.config.Config

object Component {
  def props(config: Config) = Props(classOf[Component], config)
}

class Component(config: Config) extends BaseActor {
  def receive = {
    case _ => ???
  }
}
