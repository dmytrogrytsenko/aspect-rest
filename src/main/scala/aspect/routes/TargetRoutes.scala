package aspect.routes

import aspect.common._
import aspect.common.Messages.Done
import aspect.controllers.target._
import aspect.domain.{ProjectId, TargetId}
import aspect.rest.Errors.BadRequest
import aspect.rest.JsonProtocol._
import aspect.rest.Routes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.httpx.unmarshalling.{DeserializationError, FromStringDeserializer}
import spray.json.{JsonFormat, DeserializationException, JsString, JsValue}
import spray.routing.PathMatchers.Segment
import spray.routing.PathMatcher1

object TargetRoutesJson {
  val TargetIdSegment: PathMatcher1[TargetId] = Segment.map(TargetId.apply)

  implicit val TargetIdDeserializer = new FromStringDeserializer[TargetId] {
    def apply(value: String): Either[DeserializationError, TargetId] = Right(TargetId(value))
  }

  implicit object TargetIdJsonFormat extends JsonFormat[TargetId] {
    def read(json: JsValue): TargetId = json match {
      case JsString(value) => TargetId(value)
      case _ => throw new DeserializationException("Expected TargetId as JsString")
    }
    def write(value: TargetId): JsValue = JsString(value.underlying)
  }

  import UserRoutesJson._
  import ProjectRoutesJson._

  implicit val jsonTargetItemResult = jsonFormat3(TargetItemResult.apply)
  implicit val jsonTargetListResult = jsonFormat1(TargetListResult)
  implicit val jsonTargetUserResult = jsonFormat2(TargetUserResult)
  implicit val jsonTargetProjectResult = jsonFormat3(TargetProjectResult)
  implicit val jsonTargetResult = jsonFormat4(TargetResult)
  implicit val jsonAddTargetData = jsonFormat3(AddTargetData)
  implicit val jsonAddTargetResult = jsonFormat1(AddTargetResult)
  implicit val jsonUpdateTargetData = jsonFormat2(UpdateTargetData)
}

trait TargetRoutes extends Routes {

  import ProjectRoutesJson._
  import TargetRoutesJson._
  import context.dispatcher

  val getTargetsRoute =
    path("targets") {
      get {
        authenticate(userAuthentificator) { userId =>
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
        authenticate(userAuthentificator) { userId =>
          complete {
            GetTargetController.props(userId, targetId).execute[TargetResult]
          }
        }
      }
    }

  val addTargetRoute =
    path("targets") {
      post {
        authenticate(userAuthentificator) { userId =>
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
        authenticate(userAuthentificator) { userId =>
          complete {
            RemoveTargetController.props(userId, targetId).execute[Done].map(_ => "")
          }
        }
      }
    }

  val updateTargetRoute =
    path("targets" / TargetIdSegment) { targetId =>
      put {
        authenticate(userAuthentificator) { userId =>
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
