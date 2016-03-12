package aspect

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.{GenericHttpCredentials, Authorization, RawHeader}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes._
import akka.stream.{Materializer, ActorMaterializer}
import aspect.controllers.project.{UpdateProjectData, AddProjectData}
import aspect.controllers.target.{UpdateTargetData, AddTargetData}
import aspect.controllers.user.LoginData
import aspect.domain.{TargetId, ProjectId}
import aspect.rest.Errors.{RestException, ErrorResult}
import aspect.routes.{TargetRoutesJson, ProjectRoutesJson, UserRoutesJson}
import com.ning.http.client.Response
import dispatch.{Http, Req, url}
import org.scalatest.Matchers
import org.scalatest.exceptions.TestFailedException
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait RestSupport extends Matchers with UserRoutesJson with ProjectRoutesJson with TargetRoutesJson {

  val base = "http://localhost:8888"
  val baseUrl = url("http://localhost:8888")

  object Rest {
    def login(data: LoginData) = (baseUrl / "login").POST.setJsonBody(data).execute
    def logout(token: String = null) = (baseUrl / "logout").POST.setAuth(token).execute

    def getProfile(token: String = null)
                  (implicit system: ActorSystem, materializer: Materializer) =
      Get(s"$base/profile").withAuth(token).execute //(baseUrl / "profile").GET.setAuth(token).execute

    def getProjects(token: String = null) = (baseUrl / "projects").GET.setAuth(token).execute
    def getProject(projectId: ProjectId, token: String = null) = (baseUrl / "projects" / projectId.underlying).GET.setAuth(token).execute
    def addProject(data: AddProjectData, token: String = null) = (baseUrl / "projects").POST.setAuth(token).setJsonBody(data).execute
    def removeProject(projectId: ProjectId, token: String = null) = (baseUrl / "projects" / projectId.underlying).DELETE.setAuth(token).execute
    def updateProject(projectId: ProjectId, data: UpdateProjectData, token: String = null) = (baseUrl / "projects" / projectId.underlying).PUT.setAuth(token).setJsonBody(data).execute
    def getTargets(projectId: ProjectId, token: String = null) = (baseUrl / "targets").GET.setQueryParameters(Map("projectId" -> Seq(projectId.underlying))).setAuth(token).execute
    def getTarget(targetId: TargetId, token: String = null) = (baseUrl / "targets" / targetId.underlying).GET.setAuth(token).execute
    def addTarget(data: AddTargetData, token: String = null) = (baseUrl / "targets").POST.setAuth(token).setJsonBody(data).execute
    def removeTarget(targetId: TargetId, token: String = null) = (baseUrl / "targets" / targetId.underlying).DELETE.setAuth(token).execute
    def updateTarget(targetId: TargetId, data: UpdateTargetData, token: String = null) = (baseUrl / "targets" / targetId.underlying).PUT.setAuth(token).setJsonBody(data).execute
  }

  implicit class RichHttpRequest(request: HttpRequest) {
    def withAuth(token: String) = {
      if (token == null) request
      else request.withHeaders(Authorization(GenericHttpCredentials("", token)))
    }

    def execute(implicit system: ActorSystem, materializer: Materializer) = {
      Await.result(akka.http.scaladsl.Http().singleRequest(request), 5.seconds)
    }
  }

  implicit class RichHttpResponse(response: HttpResponse) {
    def bodyAs[T: JsonReader] = {
      response.entity match {
        case HttpEntity.Strict(contentType, data) =>
          contentType should be (`application/json`)
          data.utf8String.parseJson.convertTo[T]
        case _ =>
          throw new IllegalArgumentException("Unexpected entity type")
      }
    }

    def to[T: JsonReader]: T = {
      try {
        response.status should be (StatusCodes.OK)
      } catch { case e: TestFailedException =>
        throw new RestException(response.toErrorResult)
      }
      response.bodyAs[T]
    }

    def toErrorResult = {
      val result = response.bodyAs[ErrorResult]
      response.status.intValue() should be (result.status.intValue())
      result
    }
  }

  implicit class RichReq(req: Req) {
    def setJsonBody[T](body: T)(implicit writer : JsonWriter[T]) = req
      .setContentType("application/json", "UTF-8")
      .setBody(body.toJson.toString())

    def setAuth(token: String) = {
      if (token == null) req
      else req.setHeaders(Map("Authorization" -> Seq(token)))
    }

    def execute: Response = Await.result(Http(req), 5.seconds)
  }

  implicit class ResponseJsonParsers(response: Response) {
    def to[T: JsonReader]: T = {
      try {
        StatusCode.int2StatusCode(response.getStatusCode) should be(StatusCodes.OK)
      } catch { case e: TestFailedException =>
        throw new RestException(response.toErrorResult)
      }
      response.getResponseBody.parseJson.convertTo[T]
    }

    def toErrorResult = {
      val result = response.getResponseBody.parseJson.convertTo[ErrorResult]
      response.getStatusCode should be(result.status.intValue())
      result
    }
  }
}
