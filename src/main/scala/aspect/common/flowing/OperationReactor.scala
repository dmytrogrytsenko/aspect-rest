package aspect.common.flowing

import akka.actor._
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.common.flowing.Messages._

case class Operation[I, O](name: String, underlying: ActorRef, input: Input[I], output: Output[O])
  extends Reactor with DefaultInput[I] with DefaultOutput[O]

object Operation {
  def create[I, O](factory: Message => Props)(implicit context: ActorContext) = {
    val name = this.getClass.getSimpleName
    val underlying = OperationReactor.props(factory).create(name)
    val input = Input.default[I](name)
    val output = Output.default[O](name)
    Operation(name, underlying, input, output)
  }
}

object OperationReactor {
  def props(factory: Message => Props) =
    Props(classOf[OperationReactor], factory)
}

class OperationReactor(factory: Message => Props) extends BaseActor {
  val input = Endpoint.createDefaultInput
  val output = Endpoint.createDefaultOutput

  def receive = {
    case Start => request()
  }

  def request() = {
    val reqId = CorrelationId.generate
    input !! Request(reqId)
    become(requesting(reqId))
  }

  def requesting(reqId: CorrelationId): Receive = {
    case Handle(`reqId`, msg) =>
      input !! Acknowledge(reqId)
      val operation = watch(factory(msg).create)
      become(handling(reqId, operation))
  }

  def handling(reqId: CorrelationId, operation: ActorRef): Receive = {
    case result: Message =>
      val sndId = CorrelationId.generate
      output !! Send(sndId, result)
      become(sending(sndId))
    case Status.Failure(e) =>
      ???
    case Terminated(actor) =>
      ???
    case ReceiveTimeout =>
      ???
  }

  def sending(sndId: CorrelationId): Receive = {
    case Accepted(`sndId`) => request()
  }
}
