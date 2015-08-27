package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{BaseActor, NodeSingleton}
import aspect.common.mongo.MongoStorage
import aspect.domain.Session
import aspect.mongo.SessionCollection
import reactivemongo.api.DB

case class AddSession(session: Session)
case class SessionAdded(token: String)

case class RemoveSession(token: String)
case class SessionRemoved(token: String)

case class GetSession(token: String)
case class SessionFound(session: Session)
case class SessionNotFound(token: String)

case class Activity(token: String)
case class ActivityCompleted(token: String)

object SessionRepository extends NodeSingleton[SessionRepository]

class SessionRepository extends BaseActor {

  import SessionCollection._
  import context.dispatcher

  implicit val db: DB = MongoStorage.connect.db("aspect")

  def receive = {
    case Start => ensureIndexes

    case AddSession(session) =>
      add(session) map (_ => SessionAdded(session.token)) pipeTo sender()

    case RemoveSession(token) =>
      remove(token) map (_ => SessionRemoved(token)) pipeTo sender()

    case GetSession(token) =>
      get(token) map {
        case Some(session) => SessionFound(session)
        case None => SessionNotFound(token)
      } pipeTo sender()

    case Activity(token) =>
      activity(token) map { _ =>
        log.debug("ACTIVITY COMPLETED")
        ActivityCompleted(token)
      } pipeTo sender()
  }
}
