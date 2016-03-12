package aspect.rest

import akka.http.scaladsl.model.StatusCodes._
import aspect.common.actors.SingleUseActor
import aspect.rest.Errors.{RestException, ErrorResult}

trait Controller extends SingleUseActor {
  override def complete(msg: Any) = super.complete(OK -> msg)
  def failure(result: ErrorResult): Unit = failure(new RestException(result))
}

