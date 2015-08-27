package aspect.rest

import java.util.concurrent.TimeUnit

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

import scala.concurrent.duration._

class RestSettingsImpl(config: Config) extends Extension {
  private[this] val restSettings = config.getConfig("aspect.rest")
  val interface = restSettings.getString("interface")
  val port = restSettings.getInt("port")
  val defaultTimeout = restSettings.getDuration("defaultTimeout", TimeUnit.MILLISECONDS).milliseconds
}

object RestSettings extends ExtensionId[RestSettingsImpl] with ExtensionIdProvider {
  override def lookup() = RestSettings
  override def createExtension(system: ExtendedActorSystem) = new RestSettingsImpl(system.settings.config)
}