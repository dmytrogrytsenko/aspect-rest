package aspect.rest

import akka.actor.Props
import akka.actor.SupervisorStrategy._
import akka.io.IO
import akka.util.Timeout
import aspect.common._
import aspect.common.actors.{NodeSingleton1, BaseActor}
import aspect.common.Messages.Start
import aspect.common.config.Settings
import aspect.routes._
import spray.can.Http
import spray.can.Http._
import spray.routing._

import scala.concurrent.duration.FiniteDuration

object RestService extends NodeSingleton1[RestService, RestSettings]

class RestService(settings: RestSettings) extends BaseActor with HttpService with Jasonify
  with WebRoutes
  with UserRoutes
  with ProjectRoutes
  with TargetRoutes
  with FeedRoutes {

  implicit val timeout: Timeout = Timeout(settings.defaultTimeout)

  val route = userRoutes ~ projectRoutes ~ webRoutes ~ targetRoutes ~ feedRoutes

  def actorRefFactory = context

  def receive = {
    case Start => IO(Http)(context.system) !! Http.Bind(self, settings.interface, settings.port)
    case _: Bound => become(runRoute(route))
  }

  override def supervisorStrategy = stoppingStrategy
}

trait RestSettings extends Settings {
  val interface = get[String]("interface")
  val port = get[Int]("port")
  val defaultTimeout = get[FiniteDuration]("defaultTimeout")
}
