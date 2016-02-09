package aspect.experimental.messaging

import akka.actor.{ActorContext, ActorRef, Props}
import akka.event.LoggingAdapter
import aspect.common._
import aspect.common.Messages.Start
import aspect.experimental.messaging.Messages._

import scala.util.Try

case class Printer(underlying: ActorRef, input: Input[Int]) extends Reactor

object Printer {
  def create(name: String)(implicit context: ActorContext, log: LoggingAdapter): Printer = {
    val input = Input.create[Int](name)
    val underlying = PrinterActor.props(input).create(name)
    val printer = Printer(underlying, input)
    input ! LinkOutput(printer)
    printer
  }
}

object PrinterActor {
  def props(input: Input[Int]) = Props(classOf[PrinterActor], input)
}

class PrinterActor(input: Input[Int]) extends ReactorActor {
  def receive = {
    case Start => request()
  }

  def request() = {
    val requestId = CorrelationId.generate
    input ! Request(requestId)
    become(waitingForMessage(requestId))
  }

  def waitingForMessage(requestId: CorrelationId): Receive = {
    case Handle(`requestId`, msg) if msg.has[Int]("value") =>
      val value = msg.as[Int]("value")
      input ! Accepted(requestId)
      handle(value)
      input ! Completed(requestId)
      request()
    case Handle(`requestId`, msg) =>
      val e = new IllegalArgumentException("Incorrect value type.")
      log.error(e, s"Handle failed.")
      input ! Failed(requestId, e)
      request()
    case Failed(`requestId`, e) =>
      log.error(e, s"Request $requestId failed.")
      request()
  }

  def handle(value: Int): Unit = {
    log.info(s"\n\n=======================> Printer: $value\n\n")
    Thread.sleep(1000)
  }

}
