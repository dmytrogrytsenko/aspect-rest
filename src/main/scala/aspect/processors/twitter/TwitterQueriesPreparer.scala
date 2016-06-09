package aspect.processors.twitter

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import com.typesafe.config.Config

object TwitterQueriesPreparer {
  def props(config: Config) = Props(classOf[TwitterQueriesPreparer], config)
}

class TwitterQueriesPreparer(config: Config) extends BaseActor {
  def receive = {
    case Start => ???
  }
}