package aspect.processors

import java.util.concurrent.TimeUnit

import akka.actor.{ExtendedActorSystem, ExtensionIdProvider, ExtensionId, Extension}
import aspect.domain.twitter.TwitterSearchBalanceRoute
import aspect.domain.twitter.TwitterSearchBalanceRoute.{Backward, Oldest, Adaptive}
import com.typesafe.config.Config

import scala.concurrent.duration._

case class TwitterSearchBalancingSettings(config: Config) {
  val adaptive = config.getInt("adaptive")
  val oldest = config.getInt("oldest")
  val backward = config.getInt("backward")
  def get(route: TwitterSearchBalanceRoute) = route match {
    case Adaptive => adaptive
    case Oldest => oldest
    case Backward => backward
  }
}

case class IntervalAdoptationSettings(config: Config) {
  val initial = config.getDuration("initial", TimeUnit.MILLISECONDS).milliseconds
  val factor = config.getDouble("factor")
  val min = config.getDuration("min", TimeUnit.MILLISECONDS).milliseconds
  val max = config.getDuration("max", TimeUnit.MILLISECONDS).milliseconds
}

case class ProcessingSettings(config: Config) {
  val success = IntervalAdoptationSettings(config.getConfig("success"))
  val error = IntervalAdoptationSettings(config.getConfig("error"))
}

class TwitterSearchSettingsImpl(config: Config) extends Extension {
  private[this] val settings = config.getConfig("aspect.twitter.search")
  val requestLimit = settings.getInt("requestLimit")
  val adaptiveLimit = settings.getInt("adaptiveLimit")
  val balancing = TwitterSearchBalancingSettings(config.getConfig("balancing"))
  val forward = ProcessingSettings(settings.getConfig("forward"))
  val backward = ProcessingSettings(settings.getConfig("backward"))
}

object TwitterSearchSettings extends ExtensionId[TwitterSearchSettingsImpl] with ExtensionIdProvider {
  override def lookup() = TwitterSearchSettings
  override def createExtension(system: ExtendedActorSystem) = new TwitterSearchSettingsImpl(system.settings.config)
}