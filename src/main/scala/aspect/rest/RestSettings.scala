package aspect.rest

import java.util.concurrent.TimeUnit

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

import scala.concurrent.duration._

case class EndpointSettings(config: Config) {
  val interface = config.getString("interface")
  val port = config.getInt("port")
  val defaultTimeout = config.getDuration("defaultTimeout", TimeUnit.MILLISECONDS).milliseconds
}

class RestSettingsImpl(config: Config) extends Extension {
  private[this] val restSettings = config.getConfig("aspect.rest")
  val endpoint = EndpointSettings(restSettings)
}

object RestSettings extends ExtensionId[RestSettingsImpl] with ExtensionIdProvider {
  override def lookup() = RestSettings
  override def createExtension(system: ExtendedActorSystem) = new RestSettingsImpl(system.settings.config)
}