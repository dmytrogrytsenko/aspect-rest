package aspect.common.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

trait BasicReaders {
  implicit object ConfigSettingsReader extends SettingsReader[Config] {
    def read(config: Config, name: String): Config = config.getConfig(name)
  }

  implicit object SettingsSettingsReader extends SettingsReader[Settings] {
    def read(config: Config, name: String): Settings = Settings(config.getConfig(name))
  }

  implicit object IntSettingsReader extends SettingsReader[Int] {
    def read(config: Config, name: String): Int = config.getInt(name)
  }

  implicit object StringSettingsReader extends SettingsReader[String] {
    def read(config: Config, name: String): String = config.getString(name)
  }

  implicit object FiniteDurationSettingsReader extends SettingsReader[FiniteDuration] {
    def read(config: Config, name: String): FiniteDuration =
      FiniteDuration(config.getDuration(name, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
  }

  implicit def optionSettingsReader[T](implicit reader: SettingsReader[T]) =
    new SettingsReader[Option[T]] {
      def read(config: Config, name: String): Option[T] =
        if (config.hasPath(name)) Some(reader.read(config, name)) else None
    }

  implicit object stringListSettingsReader extends SettingsReader[List[String]] {
    def read(config: Config, name: String): List[String] =
      config.getStringList(name).asScala.toList
  }

  implicit def mapSettingsReader[T](implicit reader: SettingsReader[T]) =
    new SettingsReader[Map[String, T]] {
      def read(config: Config, name: String): Map[String, T] = {
        val cfg = config.getConfig(name)
        val keys = cfg.entrySet().asScala.map(_.getKey).toSet
        keys.map(key => key -> reader.read(cfg, key)).toMap
      }
    }

  implicit def settingsSettingsReader[T <: Settings](implicit tag: ClassTag[T]) =
    new SettingsReader[T] {
      def read(config: Config, name: String): T =
        Settings.create[T](config.getConfig(name))
    }
}
