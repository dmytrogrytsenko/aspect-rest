package aspect

import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.{TimeUnit, ThreadLocalRandom}

import akka.actor._
import akka.cluster.{Cluster, MemberStatus}
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout
import aspect.common.Messages.Start
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, TimeoutException, Future, ExecutionContext}
import scala.reflect.ClassTag
import scala.util.Failure

package object common {

  type Tagged[U] = { type Tag = U }
  type @@[T, U] = T with Tagged[U]

  def newUUID = UUID.randomUUID().toString
  def randomInt(bound: Int) = ThreadLocalRandom.current().nextInt(bound)

  def digest(algorithm: String, value: String) =
    MessageDigest
      .getInstance(algorithm)
      .digest(value.getBytes)
      .map("%02x".format(_))
      .mkString

  def md5(value: String) = digest("MD5", value)

  def sha256(value: String) = digest("SHA-256", value)

  def adler32sum(value: String): Int = {
    var a = 1
    var b = 0
    value.getBytes.foreach(char => {
      a = (char + a) % 65521
      b = (b + a) % 65521
    })
    (b << 16) + a
  }

  sealed trait SHARD

  type Shard = Int @@ SHARD

  object Shard {
    val count = 1024
    val all = (0 until count).map(_.asInstanceOf[Shard]).toSet
  }

  implicit class AnyOps[T](value: T) {
    def ≠(operand: T) = value != operand
  }

  implicit class RichString(value: String) {
    def toShard = (adler32sum(sha256(value)) % Shard.count).asInstanceOf[Shard]
  }

  implicit class OptionIdOps[T](value: T) {
    def some: Option[T] = Some(value)
  }

  implicit class PipedObject[T](value: T) {
    def ~>[R] (f: T => R) = f(this.value)
    def pipe[R](f: T => R) = ~>(f)
  }

  implicit class PipedFunc[T, R](f: T => R) {
    def <~[Z](v: Z => T): Z => R = x => f(v(x))
    def <~(v: T): R = f(v)
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
    def -(operand: DateTime): FiniteDuration = FiniteDuration(value.getMillis - operand.getMillis, TimeUnit.MILLISECONDS)
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

    def execute[T](implicit tag: ClassTag[T],
                   context: ActorContext,
                   executionContext: ExecutionContext,
                   timeout: Timeout): Future[T] =
      (context.actorOf(props) ? Start flatMap normalizeAskResult).mapTo[T]
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

  implicit class RichActorRef(underlying: ActorRef) {
    def on(address: Address)(implicit context: ActorContext) =
      context.actorSelection(RootActorPath(address) / underlying.path.elements)
    def !!(message: Any)(implicit sender: ActorRef = Actor.noSender, log: LoggingAdapter): Unit = {
      log.debug(s"send to ${underlying.path.toStringWithoutAddress} message $message")
      underlying.!(message)(sender)
    }
    def >>(message: Any)(implicit context: ActorContext, log: LoggingAdapter) = {
      log.debug(s"forward to ${underlying.path.toStringWithoutAddress} message $message")
      underlying.forward(message)
    }
    def ??(message: Any)(implicit timeout: Timeout, log: LoggingAdapter): Future[Any] = {
      log.debug(s"ask ${underlying.path.toStringWithoutAddress} for $message")
      underlying.?(message)(timeout)
    }
  }

  implicit class RichActorSelection(val underlying: ActorSelection) {
    def !!(message: Any)(implicit sender: ActorRef = Actor.noSender, log: LoggingAdapter) = {
      log.debug(s"send to ${underlying.pathString} message $message")
      underlying.!(message)(sender)
    }
    def >>(message: Any)(implicit context: ActorContext, log: LoggingAdapter) = {
      log.debug(s"forward to ${underlying.pathString} message $message")
      underlying.forward(message)
    }
    def ??(message: Any)(implicit timeout: Timeout, log: LoggingAdapter): Future[Any] = {
      log.debug(s"ask ${underlying.pathString} for $message")
      underlying.?(message)(timeout)
    }
  }
}
