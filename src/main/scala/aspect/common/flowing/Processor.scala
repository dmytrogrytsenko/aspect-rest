package aspect.common.flowing

import akka.actor.{ActorRef, Cancellable, Props}
import aspect.common._
import aspect.common.Messages.{Start, Tick}
import aspect.common.actors.BaseActor
import aspect.common.config.Settings
import aspect.common.flowing.Messages._
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration

object Processor {
  def props(config: Config) = Props(classOf[Processor], config)
}

case class ProcessorSettings(config: Config) extends Settings {
  val input = get[Config]("input")
  val output = get[Config]("output")
  val operation = get[Config]("operation")
  val interval = get[FiniteDuration]("interval")
  val timeout = get[FiniteDuration]("timeout")
}

class Processor(settings: ProcessorSettings) extends BaseActor {
//  case class Response(body: Any)
//
//  private var tick: Option[Cancellable] = None
//
//  override def preStart(): Unit = {
//    super.preStart()
//    tick.foreach(_.cancel())
//    tick = Some(schedule(settings.interval, Tick))
//  }
//
//  override def postStop(): Unit = {
//    tick.foreach(_.cancel())
//    super.postStop()
//  }
//
  def receive = idle

  def idle: Receive = {
    //case Tick => request(RequestId.generate)
    case _ => ???
  }
//
//  def request(rid: RequestId) = {
//    input ! Request(rid)
//    become(waitingForHandle(rid))
//  }
//
//  def waitingForHandle(rid: CorrelationId): Receive = {
//    case Handle(`rid`, inMsgs) =>
//      input ! Started(rid)
//      val handlerProps = OperationFactory.create(settings.handler, inMsgs)
//      val startTime = DateTime.now
//      handlerProps.execute[Any] map Response pipeTo self
//      become(waitingForHandleCompleted(rid, startTime, inMsgs))
//    case Tick =>
//      request(rid)
//  }
//
//  def waitingForHandleCompleted(rid: CorrelationId, startTime: DateTime, inMsgs: List[Message]): Receive = {
//    case Response(body) =>
//      val sid = CorrelationId.generate
//      output ! Send(sid, outMsgs)
//      become(waitingForSendCompleted(rid, sid))
//    case Tick =>
//      if (DateTime.now > startTime + settings.timeout) {
//        //failed with timeout
//      }
//  }
//
//  def waitingForSendCompleted(rid: CorrelationId, sid: CorrelationId): Receive = {
//    case Completed(`sid`) =>
//      input ! Completed(rid)
//    case Failed(`sid`, error) =>
//      input ! Failed(rid, error)
//    case Tick =>
//      output ! IsAlive(sid)
//    case Pending(sid) =>
//      tryCount = 0
//  }
}
