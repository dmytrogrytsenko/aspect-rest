package aspect.experimental

import akka.actor._
import aspect.common.actors.BaseActor

import scala.annotation.tailrec
import scala.collection.immutable.Queue

//case class Initialize(config: Config)
//case class LinkSource(source: ActorRef)
case class Message(id: String, parts: Map[String, Any])
case class Send(id: String, messages: Queue[Message])
case class Accepted(id: String)
case class Pending(id: String)
case class Ready(id: String, amount: Int)
case class Received(id: String, messages: Queue[Message])

class Exchange extends BaseActor {

  case class ProducerRequest(id: String, producer: ActorRef, var messages: Queue[Message])
  case class ConsumerRequest(id: String, consumer: ActorRef, amount: Int, var messages: Queue[Message] = Queue.empty)

  var producerRequests = Queue.empty[ProducerRequest]
  var consumerRequests = Queue.empty[ConsumerRequest]

  def receive = {
    case Send(id, Queue()) =>
      sender() ! Accepted(id)
    case Ready(id, amount) if amount <= 0 =>
      sender() ! Received(id, Queue.empty)
    case Send(id, messages) =>
      producerRequests :+= ProducerRequest(id, sender(), messages)
      process()
    case Ready(id, amount) =>
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
  implicit class MessagingActorRef(source: ActorRef) {
    def ~>(sink: ActorRef) = {
      sink ! LinkSource(source)
      sink
    }
  }
}

import Messaging._

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
      natural ~> intToString ~> sleeper ~> printer
  }
}
*/