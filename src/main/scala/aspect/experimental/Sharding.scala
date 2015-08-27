package aspect.experimental

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.common.actors.{NodeSingleton1, SingleUseActor, BaseActor}
import aspect.domain.{UserId, User}
import aspect.repositories.{UserNotFoundById, UserFoundById, FindUserById, UserRepository}

trait Sharding[K, V] extends BaseActor {

  val redundancy = 3
  val role: Option[String]

  var items: Map[K, V] = Map.empty

  def initializer(key: K): Props
  def identify: PartialFunction[Any, K]
  def handle(item: V): PartialFunction[Any, Any]

  def receive = {
    case msg if identify.isDefinedAt(msg) => ???
  }

  def holders(key: K): List[Int] = ???
}

case class GetUser(userId: UserId)
case class RenameUser(userId: UserId, newName: String)

object UserSharding extends NodeSingleton1[UserSharding, Option[String]]

class UserSharding(val role: Option[String]) extends Sharding[UserId, User] {
  def initializer(userId: UserId) = UserShardingInitializer.props(userId)

  def identify = {
    case msg: GetUser => msg.userId
    case msg: RenameUser => msg.userId
  }

  def handle(user: User) = {
    case GetUser(userId) => user
    case RenameUser(userId, newName) => user.copy(name = newName)
  }
}

object UserShardingInitializer {
  def props(userId: UserId) = Props(classOf[UserShardingInitializer], userId.underlying)
}

class UserShardingInitializer(userId: UserId) extends SingleUseActor {
  def receive = {
    case Start => UserRepository.endpoint ! FindUserById(userId)
    case UserFoundById(user: User) => complete(user)
    case UserNotFoundById(`userId`) => failure(new NoSuchElementException("User not found"))
  }
}
