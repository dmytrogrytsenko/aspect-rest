package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{NodeSingleton1, BaseActor}
import aspect.domain.{UserId, User}
import aspect.mongo.UserCollection
import reactivemongo.api.DB

case class FindUserById(userId: UserId)
sealed trait FindUserByIdResult
case class UserFoundById(user: User) extends FindUserByIdResult
case class UserNotFoundById(userId: UserId) extends FindUserByIdResult

case class FindUserByName(username: String)
sealed trait FindUserByNameResult
case class UserFoundByName(user: User) extends FindUserByNameResult
case class UserNotFoundByName(username: String) extends FindUserByNameResult

object UserRepository extends NodeSingleton1[UserRepository, DB]

class UserRepository(implicit val db: DB) extends BaseActor {

  import UserCollection._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case FindUserById(userId) =>
      get(userId) map {
        case Some(user) => UserFoundById(user)
        case None => UserNotFoundById(userId)
      } pipeTo sender()

    case FindUserByName(username) =>
      findUserByName(username) map {
        case Some(user) => UserFoundByName(user)
        case None => UserNotFoundByName(username)
      } pipeTo sender()
  }
}
