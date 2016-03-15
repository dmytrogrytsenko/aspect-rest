package aspect.rest

import akka.actor.SupervisorStrategy._
import akka.io.IO
import akka.util.Timeout
import aspect.common._
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.Messages.Start
import aspect.routes._
import spray.can.Http
import spray.can.Http._
import spray.routing._

object RestService extends NodeSingleton[RestService]

class RestService extends BaseActor with HttpService with Jasonify
  with WebRoutes
  with UserRoutes
  with ProjectRoutes
  with TargetRoutes
  with FeedRoutes {

  val settings = RestSettings(context.system)

  implicit val timeout: Timeout = Timeout(settings.defaultTimeout)

  val route = userRoutes ~ projectRoutes ~ webRoutes ~ targetRoutes ~ feedRoutes

  def actorRefFactory = context

  def receive = {
    case Start => IO(Http)(context.system) !! Http.Bind(self, settings.interface, settings.port)
    case _: Bound => become(runRoute(route))
  }

  override def supervisorStrategy = stoppingStrategy
}
