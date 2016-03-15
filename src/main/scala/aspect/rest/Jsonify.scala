package aspect.rest

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings.RoutingSettings
import aspect.common._
import aspect.rest.Errors.{RestException, ErrorResult, ErrorCode}
import spray.json._

import scala.util.matching.Regex

trait Jsonify extends Directives with JsonProtocol {

  val wordPattern = "[A-Z][^A-Z]*".r
  val rejectionSuffixPattern = "Rejection$".r
  val exceptionSuffixPattern = "Exception$".r

  def classToCode(suffixPattern: Regex)(value: String): ErrorCode = value
    .replace("$", "")
    .pipe(suffixPattern.replaceFirstIn(_, ""))
    .pipe(wordPattern.findAllMatchIn)
    .map(_.matched)
    .mkString("_")
    .toUpperCase

  def toCode(exception: Throwable): ErrorCode =
    classToCode(exceptionSuffixPattern)(exception.getClass.getSimpleName)

  def toCode(rejections: Seq[Rejection]): ErrorCode = rejections match {
    case AuthenticationFailedRejection(authCause, _) :: _ =>
      classToCode(rejectionSuffixPattern)(authCause.getClass.getSimpleName)
    case rejection :: _ =>
      classToCode(rejectionSuffixPattern)(rejection.getClass.getSimpleName)
    case Nil =>
      "RESOURCE_NOT_FOUND"
  }

  def toErrorResult(status: StatusCode, e: ExceptionWithErrorInfo): ErrorResult =
    ErrorResult(status, toCode(e), e.getMessage)

  def jsonifyResponse(response: HttpResponse, rejections: Seq[Rejection]): HttpResponse =
    response.entity match {
      case HttpEntity.Strict(contentType, data) =>
        val result = ErrorResult(response.status, toCode(rejections), data.utf8String)
        response.withEntity(`application/json`, result.toJson.prettyPrint)
      case _ =>
        throw new IllegalStateException("Unexpected entity type")
    }

  def jsonifyException(response: HttpResponse, exception: Throwable): HttpResponse =
    response.entity match {
      case HttpEntity.Strict(contentType, data) =>
        val result = exception match {
          case e: RestException => e.result
          case e: IllegalRequestException => toErrorResult(e.status, e)
          case e: ExceptionWithErrorInfo => toErrorResult(response.status, e)
          case _: Throwable => ErrorResult(response.status, toCode(exception), data.utf8String)
        }
        response.copy(status = result.status).withEntity(`application/json`, result.toJson.prettyPrint)
      case _ =>
        throw new IllegalStateException("Unexpected entity type")
    }

  implicit val rejectionHandler = RejectionHandler
    .newBuilder()
    .handleAll[Rejection] { rejections =>
      mapResponse(response => jsonifyResponse(response, rejections)) {
        RejectionHandler.default(rejections).getOrElse {
          complete(StatusCodes.InternalServerError)
        }
      }
    }
    .handleNotFound(complete(StatusCodes.InternalServerError))
    .result()

  implicit def exceptionHandler(implicit settings: RoutingSettings): ExceptionHandler = ExceptionHandler {
    case e if ExceptionHandler.default(settings).isDefinedAt(e) =>
      mapResponse(response => jsonifyException(response, e)) {
        ExceptionHandler.default(settings)(e)
      }
  }
}
