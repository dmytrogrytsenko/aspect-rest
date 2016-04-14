package aspect

import akka.actor.ActorSystem
import aspect.common.Messages.Start
import aspect.common.config.Settings
import com.typesafe.config.{Config, ConfigFactory}

import scala.io.StdIn._

object AspectApp extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("aspect", config)
  val settings = AppSettings(config)
  system.actorOf(AspectGuardian.props(settings.aspect), "aspect") ! Start
  while (readLine() != "exit") { }
  system.shutdown()
}

case class AppSettings(config: Config) extends Settings {
  val aspect = get[AspectSettings]("aspect")
}
