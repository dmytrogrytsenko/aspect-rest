package aspect.rest

import akka.actor.{Props, ActorContext}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.headers.{HttpChallenge, HttpCredentials}
import akka.http.scaladsl.server.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import akka.http.scaladsl.server.directives.{AuthenticationResult, Credentials}
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directives}
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import aspect.common.Messages.Start
import aspect.common._
import aspect.common.actors.BaseActor
import aspect.domain.UserId
import aspect.repositories._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait Routes extends BaseActor with Directives with SprayJsonSupport {

  implicit val timeout: Timeout

  import context.dispatcher

  implicit class RichProps(props: Props) {
    def execute[T](implicit tag: ClassTag[T],
                   context: ActorContext,
                   executionContext: ExecutionContext,
                   timeout: Timeout): Future[(StatusCode, T)] =
      (context.actorOf(props) ? Start flatMap normalizeAskResult).mapTo[(StatusCode, T)]
  }

  val challenge = HttpChallenge(scheme = "", realm = "", params = Map.empty)

  def authenticate[T](authenticator: Option[HttpCredentials] ⇒ Future[AuthenticationResult[T]]) =
    authenticateOrRejectWithChallenge(authenticator)

  def getUserIdByToken(token: String): Future[Option[UserId]] =
    SessionRepository.endpoint ? GetSession(token) flatMap normalizeAskResult flatMap {
      case SessionFound(session) =>
        SessionRepository.endpoint ? Activity(token) flatMap normalizeAskResult map { _ => Some(session.userId) }
      case SessionNotFound(`token`) => Future { None }
    }

  def tokenAuthenticator(credentials: Option[HttpCredentials]): Future[AuthenticationResult[String]] = {
    credentials match {
      case Some(c) => FastFuture.successful(AuthenticationResult.success(c.token()))
      case None => FastFuture.successful(AuthenticationResult.failWithChallenge(challenge))
    }
  }

  def userAuthenticator(credentials: Option[HttpCredentials]): Future[AuthenticationResult[UserId]] = {
    credentials match {
      case Some(c) => getUserIdByToken(c.value) map {
        case Some(userId) => AuthenticationResult.success(userId)
        case None => AuthenticationResult.failWithChallenge(challenge)
      }
      case None => FastFuture.successful(AuthenticationResult.failWithChallenge(challenge))
    }
  }

  /*
    def userAuthentificator: ContextAuthenticator[UserId] = ctx =>
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

    def tokenAuthentificator: ContextAuthenticator[String] = ctx =>
      Future {
        ctx.request.headers
          .find(_.name == "Authorization")
          .map(header => Right(header.value))
          .getOrElse(Left(credentialsMissing))
      }

    lazy val credentialsMissing = AuthenticationFailedRejection(CredentialsMissing, List.empty)
    lazy val credentialsRejected = AuthenticationFailedRejection(CredentialsRejected, List.empty)
  */
}