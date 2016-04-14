package aspect.repositories

import aspect.common._
import aspect.common.Messages.Start
import aspect.common.actors.{NodeSingleton1, BaseActor}
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

object SessionRepository extends NodeSingleton1[SessionRepository, DB]

class SessionRepository(implicit val db: DB) extends BaseActor {

  import SessionCollection._
  import context.dispatcher

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
        ActivityCompleted(token)
      } pipeTo sender()
  }
}
