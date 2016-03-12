package aspect.rest

import akka.actor.SupervisorStrategy._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.routes._

object RestService extends NodeSingleton[RestService]

class RestService extends BaseActor with Jsonify
  with WebRoutes
  with UserRoutes
  with ProjectRoutes
  with TargetRoutes
  with FeedRoutes {

  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(settings.defaultTimeout)

  val settings = RestSettings(context.system).endpoint
  val routes = userRoutes ~ projectRoutes ~ webRoutes ~ targetRoutes ~ feedRoutes

  def receive = {
    case Start =>
      Http(context.system).bindAndHandle(routes, settings.interface, settings.port)
  }

  override def supervisorStrategy = stoppingStrategy
}
