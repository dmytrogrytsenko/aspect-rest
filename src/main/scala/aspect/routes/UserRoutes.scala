package aspect.routes

import aspect.common.Messages.Done
import aspect.common._
import aspect.controllers.user._
import aspect.domain.UserId
import aspect.rest.Errors.BadRequest
import aspect.rest.JsonProtocol._
import aspect.rest.Routes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.httpx.unmarshalling.{DeserializationError, FromStringDeserializer}
import spray.json._
import spray.routing.PathMatcher1
import spray.routing.PathMatchers.Segment

object UserRoutesJson {
  val UserIdSegment: PathMatcher1[UserId] = Segment.map(UserId.apply)

  implicit val UserIdDeserializer = new FromStringDeserializer[UserId] {
    def apply(value: String): Either[DeserializationError, UserId] = Right(UserId(value))
  }

  implicit object UserIdJsonFormat extends JsonFormat[UserId] {
    def read(json: JsValue): UserId = json match {
      case JsString(value) => UserId(value)
      case _ => throw new DeserializationException("Expected UserId as JsString")
    }
    def write(value: UserId): JsValue = JsString(value.underlying)
  }

  implicit val jsonLoginData = jsonFormat2(LoginData)
  implicit val jsonLoginResult = jsonFormat1(LoginResult)
  implicit val jsonProfileResult = jsonFormat6(ProfileResult.apply)
}

trait UserRoutes extends Routes {

  import UserRoutesJson._
  import context.dispatcher

  val loginRoute =
    path("login") {
      post {
        entity(as[LoginData]) { data =>
          validate(data.login.nonEmpty, BadRequest.Validation.requiredMemberEmpty("login").message) {
            validate(data.password.nonEmpty, BadRequest.Validation.requiredMemberEmpty("password").message) {
              complete {
                LoginController.props(data).execute[LoginResult]
              }
            }
          }
        }
      }
    }

  val logoutRoute =
    path("logout") {
      post {
        authenticate(tokenAuthentificator) { token =>
          complete {
            LogoutController.props(token).execute[Done].map(_ => "")
          }
        }
      }
    }

  val profileRoute =
    path("profile") {
      get {
        authenticate(userAuthentificator) { userId =>
          complete {
            GetProfileController.props(userId).execute[ProfileResult]
          }
        }
      }
    }

  val userRoutes = loginRoute ~ logoutRoute ~ profileRoute
}
