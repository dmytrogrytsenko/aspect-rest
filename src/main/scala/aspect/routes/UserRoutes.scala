package aspect.routes

import aspect.common.Messages.Done
import aspect.common._
import aspect.controllers.user._
import aspect.domain.UserId
import aspect.rest.Errors.BadRequest
import aspect.rest.{JsonProtocol, Routes}
import spray.json._

trait UserRoutesJson extends JsonProtocol {
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

trait UserRoutes extends Routes with UserRoutesJson {

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
        authenticate(tokenAuthenticator) { token =>
          complete {
            LogoutController.props(token).execute[Done].map(_ => "")
          }
        }
      }
    }

  val profileRoute = mapRequest(r => { println(r); r }) {
    path("profile") {
      get {
        authenticate(userAuthenticator) { userId =>
          complete {
            GetProfileController.props(userId).execute[ProfileResult]
          }
        }
      }
    }
  }

  val userRoutes = loginRoute ~ logoutRoute ~ profileRoute
}
