package aspect.routes

import akka.http.scaladsl.server.PathMatcher1
import akka.http.scaladsl.server.PathMatchers._
import aspect.common._
import aspect.common.Messages.Done
import aspect.controllers.target._
import aspect.domain.{ProjectId, TargetId}
import aspect.rest.Errors.BadRequest
import aspect.rest.{JsonProtocol, Routes}
import spray.json.{DeserializationException, JsValue, JsString, JsonFormat}

trait TargetRoutesJson extends JsonProtocol with UserRoutesJson with ProjectRoutesJson {
  val TargetIdSegment: PathMatcher1[TargetId] = Segment.map(TargetId.apply)

  implicit object TargetIdJsonFormat extends JsonFormat[TargetId] {
    def read(json: JsValue): TargetId = json match {
      case JsString(value) => TargetId(value)
      case _ => throw new DeserializationException("Expected TargetId as JsString")
    }
    def write(value: TargetId): JsValue = JsString(value.underlying)
  }

  implicit val jsonTargetItemResult = jsonFormat3(TargetItemResult.apply)
  implicit val jsonTargetListResult = jsonFormat1(TargetListResult)
  implicit val jsonTargetUserResult = jsonFormat2(TargetUserResult)
  implicit val jsonTargetProjectResult = jsonFormat3(TargetProjectResult)
  implicit val jsonTargetResult = jsonFormat4(TargetResult)
  implicit val jsonAddTargetData = jsonFormat3(AddTargetData)
  implicit val jsonAddTargetResult = jsonFormat1(AddTargetResult)
  implicit val jsonUpdateTargetData = jsonFormat2(UpdateTargetData)
}

trait TargetRoutes extends Routes with TargetRoutesJson {

  import context.dispatcher

  val getTargetsRoute =
    path("targets") {
      get {
        authenticate(userAuthenticator) { userId =>
          parameters("projectId".as[ProjectId]) { projectId =>
            complete {
              GetTargetsController.props(userId, projectId).execute[TargetListResult]
            }
          }
        }
      }
    }

  val getTargetRoute =
    path("targets" / TargetIdSegment) { targetId =>
      get {
        authenticate(userAuthenticator) { userId =>
          complete {
            GetTargetController.props(userId, targetId).execute[TargetResult]
          }
        }
      }
    }

  val addTargetRoute =
    path("targets") {
      post {
        authenticate(userAuthenticator) { userId =>
          entity(as[AddTargetData]) { data =>
            validate(data.name.nonEmpty, BadRequest.Validation.requiredMemberEmpty("name").message) {
              complete {
                AddTargetController.props(userId, data).execute[AddTargetResult]
              }
            }
          }
        }
      }
    }

  val removeTargetRoute =
    path("targets" / TargetIdSegment) { targetId =>
      delete {
        authenticate(userAuthenticator) { userId =>
          complete {
            RemoveTargetController.props(userId, targetId).execute[Done].map(_ => "")
          }
        }
      }
    }

  val updateTargetRoute =
    path("targets" / TargetIdSegment) { targetId =>
      put {
        authenticate(userAuthenticator) { userId =>
          entity(as[UpdateTargetData]) { data =>
            validate(data.name.map(_.nonEmpty).getOrElse(true), BadRequest.Validation.requiredMemberEmpty("name").message) {
              complete {
                UpdateTargetController.props(userId, targetId, data).execute[Done].map(_ => "")
              }
            }
          }
        }
      }
    }

  val targetRoutes = getTargetsRoute ~ getTargetRoute ~ addTargetRoute ~ removeTargetRoute ~ updateTargetRoute
}
