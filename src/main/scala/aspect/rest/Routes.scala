package aspect.rest

import akka.pattern.ask
import akka.util.Timeout
import aspect.common._
import aspect.common.actors.BaseActor
import aspect.domain.UserId
import aspect.repositories._
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.{AuthenticationFailedRejection, HttpService}
import spray.routing.authentication.ContextAuthenticator

import scala.concurrent.Future

trait Routes extends BaseActor with HttpService {

  import context.dispatcher

  implicit val timeout: Timeout

  def getUserIdByToken(token: String): Future[Option[UserId]] =
    SessionRepository.endpoint ? GetSession(token) flatMap normalizeAskResult flatMap {
      case SessionFound(session) =>
        log.debug("SESSION FOUND")
        SessionRepository.endpoint ? Activity(token) flatMap normalizeAskResult map { _ =>
          log.debug("ACTIVITY NORMALIZED")
          Some(session.userId)
        }
      case SessionNotFound(`token`) => Future { None }
    }

  def userAuthenticator: ContextAuthenticator[UserId] = ctx =>
    Future {
      ctx.request.headers.find(_.name == "Authorization").map(_.value)
    } flatMap {
      case Some(token) => getUserIdByToken(token) map {
        case Some(userId) =>
          log.debug("USERID READY")
          Right(userId)
        case None => Left(credentialsRejected)
      }
      case None => Future.successful(Left(credentialsMissing))
    }

  def tokenAuthenticator: ContextAuthenticator[String] = ctx =>
    Future {
      ctx.request.headers
        .find(_.name == "Authorization")
        .map(header => Right(header.value))
        .getOrElse(Left(credentialsMissing))
    }

  lazy val credentialsMissing = AuthenticationFailedRejection(CredentialsMissing, List.empty)
  lazy val credentialsRejected = AuthenticationFailedRejection(CredentialsRejected, List.empty)
}