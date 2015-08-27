package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.mongo.MongoStorage
import aspect.common.actors.{BaseActor, NodeSingleton}
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

object UserRepository extends NodeSingleton[UserRepository]

class UserRepository extends BaseActor {

  import UserCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

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
