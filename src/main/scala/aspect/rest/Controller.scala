package aspect.rest

import aspect.common.actors.Operation
import aspect.rest.Errors.{RestException, ErrorResult}

trait Controller extends Operation {
  def failure(result: ErrorResult): Unit = failure(new RestException(result))
}
