package aspect

import java.util.UUID

import akka.actor._
import akka.cluster.{Cluster, MemberStatus}
import akka.pattern.ask
import akka.util.Timeout
import aspect.common.Messages.Start
import aspect.common.actors.HandlerGuardian
import org.joda.time.{DateTimeZone, DateTime}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, TimeoutException, Future, ExecutionContext}
import scala.reflect.ClassTag
import scala.util.{Random, Failure}

package object common {

  def newUUID = UUID.randomUUID().toString

  implicit class PipedObject[T](value: T) {
    def |>[R] (f: T => R) = f(this.value)
    def pipe[R](f: T => R) = |>(f)
  }

  def normalizeAskResult(msg: Any): Future[Any] = msg match {
    case Failure(exception) => Future.failed(exception)
    case Status.Failure(exception) => Future.failed(exception)
    case ReceiveTimeout => Future.failed(new TimeoutException())
    case result => Future.successful(result)
  }

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan[DateTime](_ isBefore _)

  implicit class RichDateTime(value: DateTime) {
    def >(operand: DateTime): Boolean = value isAfter operand
    def <(operand: DateTime): Boolean = value isBefore operand
    def >=(operand: DateTime): Boolean = !value.isBefore(operand)
    def <=(operand: DateTime): Boolean = !value.isAfter(operand)
    def +(operand: FiniteDuration): DateTime = value plusMillis operand.toMillis.toInt
    def -(operand: FiniteDuration): DateTime = value minusMillis operand.toMillis.toInt
    def min(operand: DateTime): DateTime = if (value < operand) value else operand
    def max(operand: DateTime): DateTime = if (value > operand) value else operand
  }

  implicit class RichFuture[T](val future: Future[T]) {
    def await(implicit timeout: Timeout) = Await.result(future, timeout.duration)

    def pipeTo(destination: ActorRef)(implicit executionContext: ExecutionContext) =
      future recover { case e =>
        println(e)
        Status.Failure(e)
      } map {
        destination ! _
      }

    def |=>(destination: ActorRef)(implicit executionContext: ExecutionContext) = pipeTo(destination)
  }

  implicit class RichProps(props: Props) {

    def create(implicit context: ActorContext) = {
      val actor = context.actorOf(props)
      actor ! Start
      actor
    }

    def create(name: String)(implicit context: ActorContext) = {
      val actor = context.actorOf(props, name)
      actor ! Start
      actor
    }

    def start(implicit context: ActorContext): Unit = HandlerGuardian.endpoint ! props

    def delegate(implicit context: ActorContext): Unit = HandlerGuardian.endpoint forward props

    def execute[T](implicit tag: ClassTag[T],
                   context: ActorContext,
                   executionContext: ExecutionContext,
                   timeout: Timeout): Future[T] =
      (HandlerGuardian.endpoint ? props flatMap normalizeAskResult).mapTo[T]
  }

  implicit class RichCluster(cluster: Cluster) {
    def leader(role: Option[String] = None) = role.fold(cluster.state.leader)(cluster.state.roleLeader)

    def members(role: Option[String] = None) = cluster.state.members.toList
      .filter(_.status == MemberStatus.up)
      .filter(member => role.fold(true)(member.hasRole))
      .sortWith(_ isOlderThan _)
  }

  implicit class RichAddress(address: Address) {
    def /(elements: Iterable[String]): ActorPath =
      elements.foldLeft(RootActorPath(address).asInstanceOf[ActorPath])(_ / _)
    def selfClone(implicit context: ActorContext): ActorSelection =
      context.actorSelection(RootActorPath(address) / context.self.path.elements)
  }

  implicit class RemoteActorRef(instance: ActorRef) {
    def on(address: Address)(implicit context: ActorContext) =
      context.actorSelection(RootActorPath(address) / instance.path.elements)
  }
}
