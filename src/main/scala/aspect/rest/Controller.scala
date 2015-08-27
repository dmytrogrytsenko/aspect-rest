package aspect.rest

import aspect.common.actors.SingleUseActor
import aspect.rest.Errors.{RestException, ErrorResult}

trait Controller extends SingleUseActor {
  def failure(result: ErrorResult): Unit = failure(new RestException(result))
}
