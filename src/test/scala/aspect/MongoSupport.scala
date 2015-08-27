package aspect

import akka.util.Timeout
import aspect.common._
import aspect.domain._
import aspect.mongo.{TargetCollection, ProjectCollection, UserCollection, SessionCollection}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import reactivemongo.api.{MongoDriver, DB}

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object MongoSupport {
  lazy val config = ConfigFactory.load()
  lazy val driver = new MongoDriver
  lazy val hosts = config.getStringList("aspect.mongo.hosts").toList
  lazy val connection = driver.connection(hosts)
  lazy val db = connection("aspect")
}

trait MongoSupport {

  implicit val timeout: Timeout
  implicit val db: DB = MongoSupport.db

  object Mongo {

    def addSession(token: String = newUUID,
                   userId: UserId = UserId.generate(),
                   createdAt: DateTime = DateTime.now,
                   lastActivityAt: DateTime = DateTime.now): Session = {
      val session = Session(token, userId, createdAt, lastActivityAt)
      import SessionCollection.SessionWriter
      SessionCollection.add(session).await
      session
    }

    def removeSession(token: String) = SessionCollection.remove(token).await

    def getSession(token: String): Option[Session] = {
      import SessionCollection.SessionReader
      SessionCollection.get(token).await
    }

    def addUser(id: UserId = UserId.generate(),
                name: String = newUUID,
                password: String = newUUID,
                email: String = newUUID,
                firstName: Option[String] = Some(newUUID),
                lastName: Option[String] = Some(newUUID),
                company: Option[String] = Some(newUUID)): User = {
      val user = User(id, name, password, email, firstName, lastName, company)
      import UserCollection.UserWriter
      UserCollection.add(user).await
      user
    }

    def removeUser(id: UserId) = {
      import UserCollection.UserIdWriter
      UserCollection.remove(id).await
    }

    def getProject(id: ProjectId): Option[Project] = {
      import ProjectCollection.ProjectIdWriter
      import ProjectCollection.ProjectReader
      ProjectCollection.get(id).await
    }

    def addProject(id: ProjectId = ProjectId.generate(),
                   userId: UserId = UserId.generate(),
                   name: String = newUUID): Project = {
      val project = Project(id, userId, name)
      import ProjectCollection.ProjectWriter
      ProjectCollection.add(project).await
      project
    }

    def removeProject(id: ProjectId) = {
      import ProjectCollection.ProjectIdWriter
      ProjectCollection.remove(id).await
    }

    def getTarget(id: TargetId): Option[Target] = {
      import TargetCollection.TargetIdWriter
      import TargetCollection.TargetReader
      TargetCollection.get(id).await
    }

    def addTarget(id: TargetId = TargetId.generate(),
                  projectId: ProjectId = ProjectId.generate(),
                  name: String = newUUID,
                  keywords: String = newUUID): Target = {
      val target = Target(id, projectId, name, keywords)
      import TargetCollection.TargetWriter
      TargetCollection.add(target).await
      target
    }

    def removeTarget(id: TargetId) = {
      import TargetCollection.TargetIdWriter
      TargetCollection.remove(id).await
    }
  }
}
