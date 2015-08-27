package aspect.rest

import akka.util.Timeout
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.routes.{TargetRoutes, ProjectRoutes, UserRoutes, WebRoutes}
import spray.routing._

import scala.concurrent.duration._

object RestService extends NodeSingleton[RestService]

class RestService extends BaseActor with HttpService with Jasonify
  with WebRoutes
  with UserRoutes
  with ProjectRoutes
  with TargetRoutes {

  val settings = RestSettings(context.system)

  implicit val timeout: Timeout = Timeout(5.seconds)

  val route = userRoutes ~ projectRoutes ~ webRoutes ~ targetRoutes

  def actorRefFactory = context

  def receive = runRoute(route)
}
