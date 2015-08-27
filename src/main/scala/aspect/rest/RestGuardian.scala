package aspect.rest

import akka.io.IO
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import spray.can.Http

object RestGuardian extends NodeSingleton[RestGuardian]

class RestGuardian extends BaseActor {

  val settings = RestSettings(context.system)

  def receive = {
    case Start =>
      val rest = RestService.create
      IO(Http)(context.system) ! Http.Bind(rest, settings.interface, settings.port)
  }
}
