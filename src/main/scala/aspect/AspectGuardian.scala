package aspect

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.common.actors.BaseActor
import aspect.common.config.Settings
import aspect.processors.twitter.{TwitterSettings, TwitterComponent}
import aspect.repositories._
import aspect.rest.{RestSettings, RestService}
import com.typesafe.config.Config
import reactivemongo.api.MongoDriver

object AspectGuardian {
  def props(settings: AspectSettings) =
    Props(classOf[AspectGuardian], settings)
}

class AspectGuardian(settings: AspectSettings) extends BaseActor {
  def receive = {
    case Start =>
      createRepositories()
      if (hasRole("twitter")) {
        TwitterComponent.create(settings.twitter)
      }
      if (hasRole("rest")) {
        RestService.create(settings.rest)
      }
  }

  def hasRole(role: String) = cluster.selfRoles.exists(r => r == role || r == "all")

  def createRepositories(): Unit = {
    import context.dispatcher
    val driver = new MongoDriver(context.system)
    val connection = driver.connection(settings.mongo.hosts)
    val db = connection.db(settings.mongo.db)
    UserRepository.create(db)
    SessionRepository.create(db)
    ProjectRepository.create(db)
    TargetRepository.create(db)
    KeywordRepository.create(db)
  }
}

case class MongoSettings(config: Config) extends Settings {
  val hosts = get[List[String]]("hosts")
  val db = get[String]("db")
}

case class AspectSettings(config: Config) extends Settings {
  val mongo = get[MongoSettings]("mongo")
  val twitter = get[TwitterSettings]("twitter")
  val rest = get[RestSettings]("rest")
}
