package aspect.common.actors

import akka.actor._
import aspect.common._
import aspect.common.Messages.Start
import aspect.common.config.Settings
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.concurrent.duration._

object Repeater {
  def props(settings: RepeaterSettings, factory: Config => Props) =
    Props(classOf[Repeater], settings, factory)
}

class Repeater(settings: RepeaterSettings,
               factory: Config => Props) extends BaseActor {
  case object Execute

  def receive = {
    case Start => wait(DateTime.now + settings.initialInterval)
  }

  def wait(startTime: DateTime): Unit = {
    val now = DateTime.now
    if (startTime >= now) self !! Execute
    else scheduleOnce(now - startTime, Execute)
    context.setReceiveTimeout(Duration.Undefined)
    become(waiting)
  }

  def waiting: Receive = {
    case Execute =>
      val operation = watch(factory(settings.operation).create("operation"))
      context.setReceiveTimeout(settings.timeout)
      become(executing(DateTime.now, operation))
  }

  def executing(startTime: DateTime, operation: ActorRef): Receive = {
    case Terminated(actor) if actor == operation => wait(startTime + settings.interval)
    case ReceiveTimeout => operation !! PoisonPill
  }
}

trait RepeaterSettings extends Settings {
  val initialInterval = get[Option[FiniteDuration]]("initialInterval").getOrElse(Duration.Zero)
  val interval = get[FiniteDuration]("interval")
  val timeout = get[FiniteDuration]("timeout")
  val operation = get[Config]("operation")
}
