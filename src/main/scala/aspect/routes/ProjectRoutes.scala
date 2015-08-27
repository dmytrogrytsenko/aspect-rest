package aspect.routes

import aspect.common._
import aspect.common.Messages.Done
import aspect.controllers.project._
import aspect.domain.ProjectId
import aspect.rest.Errors.BadRequest
import aspect.rest.JsonProtocol._
import aspect.rest.Routes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.httpx.unmarshalling.{DeserializationError, FromStringDeserializer}
import spray.json.{JsonFormat, JsValue, JsString, DeserializationException}
import spray.routing.PathMatcher1
import spray.routing.PathMatchers.Segment

object ProjectRoutesJson {
  val ProjectIdSegment: PathMatcher1[ProjectId] = Segment.map(ProjectId.apply)

  implicit val ProjectIdDeserializer = new FromStringDeserializer[ProjectId] {
    def apply(value: String): Either[DeserializationError, ProjectId] = Right(ProjectId(value))
  }

  implicit object ProjectIdJsonFormat extends JsonFormat[ProjectId] {
    def read(json: JsValue): ProjectId = json match {
      case JsString(value) => ProjectId(value)
      case _ => throw new DeserializationException("Expected ProjectId as JsString")
    }
    def write(value: ProjectId): JsValue = JsString(value.underlying)
  }

  import UserRoutesJson._

  implicit val jsonProjectItemResult = jsonFormat2(ProjectItemResult.apply)
  implicit val jsonProjectListResult = jsonFormat1(ProjectListResult)
  implicit val jsonProjectResult = jsonFormat2(ProjectUserResult.apply)
  implicit val jsonProjectUserResult = jsonFormat3(ProjectResult.apply)
  implicit val jsonAddProjectData = jsonFormat1(AddProjectData.apply)
  implicit val jsonAddProjectResult = jsonFormat1(AddProjectResult.apply)
  implicit val jsonUpdateProjectData = jsonFormat1(UpdateProjectData.apply)
}

trait ProjectRoutes extends Routes {

  import ProjectRoutesJson._
  import context.dispatcher

  val getProjectsRoute =
    path("projects") {
      get {
        authenticate(userAuthentificator) { userId =>
          complete {
            GetProjectsController.props(userId).execute[ProjectListResult]
          }
        }
      }
    }

  val getProjectRoute =
    path("projects" / ProjectIdSegment) { projectId =>
      get {
        authenticate(userAuthentificator) { userId =>
          complete {
            GetProjectController.props(userId, projectId).execute[ProjectResult]
          }
        }
      }
    }

  val addProjectRoute =
    path("projects") {
      post {
        authenticate(userAuthentificator) { userId =>
          entity(as[AddProjectData]) { data =>
            validate(data.name.nonEmpty, BadRequest.Validation.requiredMemberEmpty("name").message) {
              complete {
                AddProjectController.props(userId, data).execute[AddProjectResult]
              }
            }
          }
        }
      }
    }

  val removeProjectRoute =
    path("projects" / ProjectIdSegment) { projectId =>
      delete {
        authenticate(userAuthentificator) { userId =>
          complete {
            RemoveProjectController.props(userId, projectId).execute[Done].map(_ => "")
          }
        }
      }
    }

  val updateProjectRoute =
    path("projects" / ProjectIdSegment) { projectId =>
      put {
        authenticate(userAuthentificator) { userId =>
          entity(as[UpdateProjectData]) { data =>
            validate(data.name.map(_.nonEmpty).getOrElse(true), BadRequest.Validation.requiredMemberEmpty("name").message) {
              complete {
                UpdateProjectController.props(userId, projectId, data).execute[Done].map(_ => "")
              }
            }
          }
        }
      }
    }

  val projectRoutes = getProjectsRoute ~ getProjectRoute ~ addProjectRoute ~ removeProjectRoute ~ updateProjectRoute
}
