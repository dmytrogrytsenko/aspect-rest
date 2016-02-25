package aspect.rest

import akka.util.Timeout
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.routes._
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

  def receive = runRoute(route)
}
