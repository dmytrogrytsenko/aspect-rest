package aspect.experimental.flowing

import akka.actor._
import aspect.common.Messages.Start
import aspect.common._
import aspect.common.actors.BaseActor
import aspect.experimental.flowing.Messages._

import scala.concurrent.duration._

case class Operation1[I, O](name: String, underlying: ActorRef, input: Input[I], output: Output[O])
  extends Reactor with DefaultInput[I] with DefaultOutput[O]

object Operation1 {
  def create[I, O](factory: Message => Props)(implicit context: ActorContext) = {
    val name = this.getClass.getSimpleName
    val underlying = OperationReactor.props(factory).create(name)
    val input = Input.default[I](name)
    val output = Output.default[O](name)
    Operation1(name, underlying, input, output)
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
    context.setReceiveTimeout(Duration.Inf)
    val reqId = CorrelationId.generate
    input !! Request(reqId)
    become(requesting(reqId))
  }

  def requesting(reqId: CorrelationId): Receive = {
    case Handle(`reqId`, msg) =>
      input !! Acknowledge(reqId)
      val operation = watch(factory(msg).create)
      context.setReceiveTimeout(5.seconds)
      become(handling(reqId, operation))
    case Terminated(actor) =>
      log.warning("Expired handling termination.")
  }

  def handling(reqId: CorrelationId, operation: ActorRef): Receive = {
    case result: Message =>
      val sndId = CorrelationId.generate
      output !! Send(sndId, result)
      become(sending(sndId))
    case Status.Failure(e) =>
      log.error(e, "Handling failed.")
      request()
    case Terminated(actor) if actor == operation =>
      log.error("Handling terminated.")
      request()
    case Terminated(actor) =>
      log.warning("Expired handling termination.")
    case ReceiveTimeout =>
      log.error("Handling timed out.")
      request()
  }

  def sending(sndId: CorrelationId): Receive = {
    case Accepted(`sndId`) => request()
    case Terminated(actor) => log.warning("Expired handling termination.")
  }
}
