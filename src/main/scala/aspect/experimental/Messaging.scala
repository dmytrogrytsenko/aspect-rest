package aspect.experimental

import akka.actor._
import aspect.common.actors.BaseActor

import scala.annotation.tailrec
import scala.collection.immutable.Queue

object Messaging {
  case class Link(exchange: ActorRef)

  case class MessageId(underlying: String) extends AnyVal
  case class Message(id: MessageId, parts: Map[String, Any])

  case class CorrelationId(underlying: String) extends AnyVal

  case class Send(id: CorrelationId, messages: Queue[Message])
  case class Pending(id: CorrelationId)
  case class Accepted(id: CorrelationId)

  case class Demand(id: CorrelationId, amount: Int)
  case class Received(id: CorrelationId, messages: Queue[Message])
  case class Handling(id: CorrelationId)
  case class Acknowledge(id: CorrelationId)
}

import Messaging._

class Input extends BaseActor {
  def receive = {
    case Link(exchange) => become(linked(exchange))
  }
  def linked(exchange: ActorRef): Receive = {
    case _ => ???
  }
}

class Output extends BaseActor {
  def receive = {
    case Link(exchange) => become(linked(exchange))
  }
  def linked(exchange: ActorRef): Receive = {
    case msg @ Send(id, messages) => exchange ! msg
    case Accepted(id) =>
    //case Flush => ???
  }
}

trait ProcessingItem extends BaseActor {
  def inputs: Map[String, Input]
  def outputs: Map[String, Output]
  def receive = ???
}

class Exchange extends BaseActor {

  case class ProducerRequest(id: CorrelationId, producer: ActorRef, var messages: Queue[Message])
  case class ConsumerRequest(id: CorrelationId, consumer: ActorRef, amount: Int, var messages: Queue[Message] = Queue.empty)

  var producerRequests = Queue.empty[ProducerRequest]
  var consumerRequests = Queue.empty[ConsumerRequest]

  def receive = {
    case Send(id, Queue()) =>
      sender() ! Accepted(id)
    case Demand(id, amount) if amount <= 0 =>
      sender() ! Received(id, Queue.empty)
    case Send(id, messages) =>
      producerRequests :+= ProducerRequest(id, sender(), messages)
      process()
    case Demand(id, amount) =>
      consumerRequests :+= ConsumerRequest(id, sender(), amount)
      process()
  }

  @tailrec
  private def process(): Unit = (producerRequests, consumerRequests) match {
    case (ProducerRequest(id, producer, Queue()) +: _, _) =>
      producerRequests = producerRequests.tail
      producer ! Accepted(id)
      process()
    case (_, ConsumerRequest(id, consumer, amount, messages) +: _) if messages.length >= amount =>
      consumerRequests = consumerRequests.tail
      consumer ! Received(id, messages)
      process()
    case (p +: _, c +: _) =>
      val count = Math.min(p.messages.length, c.amount - c.messages.length)
      c.messages ++= p.messages.take(count)
      p.messages = p.messages.drop(count)
      process()
    case _ => // nothing to process
  }
}
/*
object Messaging {
  implicit class MessagingProps(props: Props) {
    def create(name: String, parentConfig: Config)(implicit context: ActorContext) = {
      val config = parentConfig.getConfig(name)
      val actor = context.actorOf(props, name)
      actor ! Initialize(config)
      actor
    }
  }
}

class NaturalNumberGenerator extends BaseActor {
  var value: Int = 1
  def receive = {
    case Initialize(config) =>
    case Request(amount) =>
      (value until value + amount) foreach { x => sender() ! Message(Map("default" -> x)) }
      value += amount
  }
}

class IntToStringConverter extends BaseActor {
  var source: ActorRef = null
  var requests: Queue[(ActorRef, Int)] = Queue.empty
  var messages: Queue[Message] = Queue.empty

  def receive  = {
    case Initialize(config) =>
    case LinkSource(actor) =>
      source = actor
      watch(source)
    case Terminated(actor) if actor == source =>
      stop()
    case Request(amount) if amount > 0 =>
      source ! Request(amount)
      requests.enqueue(sender() -> amount)
      handle()
    case msg @ Message(parts) =>
      messages +:= msg
      handle()
  }

  def handle() = {}
}

class Sleeper extends BaseActor {
  def receive = {
    case _ =>
  }
}

class Printer extends BaseActor {
  var source: ActorRef = null
  def receive = {
    case Initialize(config) =>
    case LinkSource(actor) =>
      source = actor
      watch(source)
      source ! Request(1)
    case Message(parts) =>
      println(parts("default").toString)
      //sender() ! Acknowledge(Set(id))
      source ! Request(1)
    case Terminated(actor) if actor == source => stop()
  }
}

class Flow extends BaseActor {
  def receive = {
    case Initialize(config) =>
      val natural = Props[NaturalNumberGenerator].create("natural", config)
      val intToString = Props[IntToStringConverter].create("intToString", config)
      val sleeper = Props[Sleeper].create("sleeper", config)
      val printer = Props[Printer].create("printer", config)
      natural ~> split; split.out1 ~>              sum.in1; sum ~> intToString ~> sleeper ~> printer
                        split.out2 ~> increment ~> sum.in2
  }
}
*/