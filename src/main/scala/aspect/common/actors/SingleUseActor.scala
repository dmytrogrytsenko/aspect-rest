package aspect.common.actors

import java.util.concurrent.TimeoutException

import akka.actor.{Status, ReceiveTimeout, ActorRef}
import akka.event.LoggingReceive
import aspect.common.Messages.Start

import scala.concurrent.duration._
import scala.util.Try

trait SingleUseActor extends BaseActor {

  val receiveTimeout = 5.seconds

  var started: Boolean = false
  var originalSender: ActorRef = null

  context.setReceiveTimeout(receiveTimeout)

  def start(): Unit = {}

  def answer(msg: Any) = originalSender ! msg

  def complete(msg: Any) = {
    answer(msg)
    stop()
  }

  def failure(exception: Throwable) = complete(Status.Failure(exception))

  override def aroundReceive(receive: Receive, msg: Any) = super.aroundReceive(handle(receive), msg)

  def handle(body: Receive): Receive = LoggingReceive {
    case Start if !started =>
      originalSender = sender()
      if (body.isDefinedAt(Start)) answerableHandle(body, Start)
      started = true
    case msg if body.isDefinedAt(msg) => answerableHandle(body, msg)
    case msg: Status.Failure => complete(msg)
    case ReceiveTimeout => failure(new TimeoutException())
  }

  def answerableHandle(body: Receive, msg: Any) = Try { body(msg) } recover { case e => failure(e) }
}
