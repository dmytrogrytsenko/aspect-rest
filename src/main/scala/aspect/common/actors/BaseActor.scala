package aspect.common.actors

import akka.actor.{Cancellable, ActorRef, ActorLogging, Actor}
import akka.cluster.Cluster
import akka.event.LoggingReceive
import aspect.common._

import scala.concurrent.duration.FiniteDuration

trait BaseActor extends Actor with ActorLogging {

  override implicit val log = akka.event.Logging(this)

  lazy val cluster = Cluster(context.system)

  override def aroundReceive(body: Receive, msg: Any): Unit = {
    val logMessage = s"${this.context.self.path.toStringWithoutAddress} received ${if (body.isDefinedAt(msg)) "" else "un"}handled message $msg"
    if (body.isDefinedAt(msg)) log.debug(logMessage) else log.error(logMessage)
    try {
      super.aroundReceive(body, msg)
    } catch {
      case e: Throwable =>
        log.error(e, s"handling $msg throws $e")
        throw e
    }
  }

  lazy val parent = context.parent

  def become(behavior: Receive, discardOld: Boolean = true) =
    context.become(behavior, discardOld)

  def unbecome() = context.unbecome()

  def stop() = context.stop(self)

  def watch(subject: ActorRef) = context.watch(subject)

  def unwatch(subject: ActorRef) = context.unwatch(subject)

  def scheduleOnce(delay: FiniteDuration,
                   message: Any,
                   receiver: ActorRef = self): Cancellable = {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(delay, receiver, message)
  }

  def schedule(initialDelay: FiniteDuration,
               interval: FiniteDuration,
               message: Any): Cancellable = {
    import context.dispatcher
    context.system.scheduler.schedule(initialDelay, interval, self, message)
  }

  def schedule(interval: FiniteDuration, message: Any): Cancellable =
    schedule(interval, interval, message)

  def clones(role: Option[String] = None) =
    cluster.members(role).map(_.address.selfClone)
}
