package aspect.common.config

import com.typesafe.config.Config

import scala.reflect.ClassTag

object Settings {
  def apply(cfg: Config): Settings =
    new Settings { val config = cfg }

  def create[T <: Settings](config: Config)(implicit tag: ClassTag[T]): T = {
    tag.runtimeClass.getConstructor(classOf[Config]).newInstance(config).asInstanceOf[T]
  }
}

trait Settings extends BasicReaders {
  def config: Config

  def as[T <: Settings](implicit tag: ClassTag[T]): T =
    Settings.create[T](config)

  def get[T](name: String)(implicit reader: SettingsReader[T]): T =
    reader.read(config, name)
}
