package aspect.processors.twitter

import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton1, Repeater, RepeaterSettings}
import aspect.common.config.Settings
import com.typesafe.config.Config

object TwitterComponent extends NodeSingleton1[TwitterComponent, TwitterSettings]

class TwitterComponent(settings: TwitterSettings) extends BaseActor {
  def receive = {
    case Start =>
      Repeater.props(settings.queriesPreparer, TwitterQueriesPreparer.props)
  }
}

case class TwitterSettings(config: Config) extends Settings {
  val queriesPreparer = get[RepeaterSettings]("queriesPreparer")
}

