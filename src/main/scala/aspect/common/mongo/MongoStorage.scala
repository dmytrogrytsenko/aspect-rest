package aspect.common.mongo

import akka.actor._
import com.typesafe.config.Config
import reactivemongo.api.MongoDriver

import scala.collection.JavaConversions._

object MongoStorage  {
  def connect(implicit context: ActorContext) = {
    val settings = MongoSettings(context.system)
    val driver = new MongoDriver(context.system)
    val connection = driver.connection(settings.hosts)
    connection
  }
}

class MongoSettingsImpl(config: Config) extends Extension {
  private[this] val mongoSettings = config.getConfig("aspect.mongo")
  val hosts = mongoSettings.getStringList("hosts").toList
}

object MongoSettings extends ExtensionId[MongoSettingsImpl] with ExtensionIdProvider {
  override def lookup() = MongoSettings
  override def createExtension(system: ExtendedActorSystem) = new MongoSettingsImpl(system.settings.config)
}
