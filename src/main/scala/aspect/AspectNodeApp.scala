package aspect

import akka.actor.{ActorSystem, Props}
import aspect.common.Messages.Start
import com.typesafe.config.ConfigFactory

import scala.io.StdIn._

object AspectNodeApp extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("AspectClusterSystem", config)
  system.actorOf(Props[AspectNodeGuardian], name = "guardian") ! Start
  while (readLine() != "shutdown") { }
  system.shutdown()
}
