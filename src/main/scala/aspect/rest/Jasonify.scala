package aspect.rest

import akka.pattern.AskTimeoutException
import aspect.common._
import aspect.rest.Errors._
import aspect.rest.JsonProtocol._
import spray.http.HttpHeaders.{`Content-Range`, Allow}
import spray.json._
import spray.http._
import spray.http.ContentTypes._
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.routing.directives.BasicDirectives
import spray.routing._
import spray.util.LoggingContext

import scala.util.matching.Regex

trait Jasonify extends BasicDirectives {
/*
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

  def toCode(rejections: List[Rejection]): ErrorCode = rejections match {
    case AuthenticationFailedRejection(authCause, _) :: _ =>
      classToCode(rejectionSuffixPattern)(authCause.getClass.getSimpleName)
    case rejection :: _ =>
      classToCode(rejectionSuffixPattern)(rejection.getClass.getSimpleName)
    case Nil => "RESOURCE_NOT_FOUND"
  }

  def toErrorResult(rejections: List[Rejection], response: HttpResponse): ErrorResult =
    ErrorResult(response.status, toCode(rejections), response.entity.asString)

  def toErrorResult(status: StatusCode, e: ExceptionWithErrorInfo)(implicit settings: RoutingSettings): ErrorResult =
    ErrorResult(status, toCode(e), e.info.summary,
      debug = if (settings.verboseErrorMessages) Some(Map("detail" -> e.info.detail)) else None)

  def toErrorResult(exception: Throwable, response: HttpResponse)
                   (implicit settings: RoutingSettings): ErrorResult = exception match {
    case e: RestException => e.result
    case e: IllegalRequestException => toErrorResult(e.status, e)
    case e: RequestProcessingException => toErrorResult(e.status, e)
    case e: ExceptionWithErrorInfo => toErrorResult(response.status, e)
    case _: Throwable => ErrorResult(response.status, toCode(exception), response.entity.asString)
  }

  implicit class RichHttpResponse(response: HttpResponse) {
    def withErrorResult(result: ErrorResult): HttpResponse =
      response.copy(status = result.status).withEntity(HttpEntity(`application/json`, result.toJson.toString()))
  }

  def jsonifyRejection(rejections: List[Rejection])
                      (response: HttpResponse): HttpResponse =
    response.withErrorResult(toErrorResult(rejections, response))

  def jsonifyException(exception: Throwable)
                      (response: HttpResponse)
                      (implicit settings: RoutingSettings): HttpResponse =
    response.withErrorResult(toErrorResult(exception, response))

  implicit val rejectionHandler = RejectionHandler {
    case rejections if RejectionHandler.Default.isDefinedAt(rejections) =>
      mapHttpResponse(jsonifyRejection(rejections))(RejectionHandler.Default(rejections))
  }

  implicit def exceptionHandler(implicit settings: RoutingSettings,
                                log: LoggingContext): ExceptionHandler = ExceptionHandler {
    case e if ExceptionHandler.default.isDefinedAt(e) =>
      mapHttpResponse(jsonifyException(e))(ExceptionHandler.default(settings, log)(e))
  }
*/
  def failed(result: ErrorResult): Route = _.complete(result.status -> result)

  implicit def exceptionHandler(implicit settings: RoutingSettings, log: LoggingContext): ExceptionHandler = ExceptionHandler {
    case e: RestException =>
      log.error(e, e.result.status.reason)
      failed(e.result)
    case e: AskTimeoutException =>
      log.error(e, StatusCodes.InternalServerError.reason)
      failed(InternalServerError.timeout)
    case e: Throwable =>
      log.error(e, StatusCodes.InternalServerError.reason)
      failed(InternalServerError.default)
  }

  implicit val rejectionHandler = RejectionHandler {

    case Nil => failed(NotFound.resourceNotFound)

    case AuthenticationFailedRejection(cause, challengeHeaders) :: _ =>
      val result = cause match {
        case CredentialsMissing  => Unauthorized.credentialsMissing
        case CredentialsRejected => Unauthorized.credentialsRejected
      }
      _.complete(result.status, challengeHeaders, result)

    case AuthorizationFailedRejection :: _ => failed(Forbidden.accessDenied)
    case CorruptRequestEncodingRejection(msg) :: _ => failed(BadRequest.corruptRequestEncoding(msg))
    case MalformedFormFieldRejection(name, msg, _) :: _ => failed(BadRequest.malformedFormField(name, msg))
    case MalformedHeaderRejection(headerName, msg, _) :: _ => failed(BadRequest.malformedHeader(headerName, msg))
    case MalformedQueryParamRejection(name, msg, _) :: _ => failed(BadRequest.malformedQueryParam(name, msg))
    case MalformedRequestContentRejection(msg, _) :: _ => failed(BadRequest.malformedRequestContent(msg))

    case rejections @ (MethodRejection(_) :: _) =>
      val methods = rejections.collect { case MethodRejection(method) => method }
      val result = MethodNotAllowed.default(methods)
      _.complete(result.status, List(Allow(methods: _*)), result)

    case rejections @ (SchemeRejection(_) :: _) =>
      val schemes = rejections.collect { case SchemeRejection(scheme) => scheme }
      failed(BadRequest.schemeNotAllowed(schemes))

    case MissingCookieRejection(cookieName) :: _ => failed(BadRequest.missingCookie(cookieName))
    case MissingFormFieldRejection(fieldName) :: _ => failed(BadRequest.missingFormField(fieldName))
    case MissingHeaderRejection(headerName) :: _ => failed(BadRequest.missingHeader(headerName))
    case MissingQueryParamRejection(paramName) :: _ => failed(BadRequest.missingQueryParam(paramName))
    case RequestEntityExpectedRejection :: _ => failed(BadRequest.requestEntityExpected)
    case TooManyRangesRejection(_) :: _ => failed(RequestedRangeNotSatisfiable.tooManyRanges)

    case UnsatisfiableRangeRejection(unsatisfiableRanges, actualEntityLength) :: _ =>
      val result = RequestedRangeNotSatisfiable.unsatisfiableRange(unsatisfiableRanges.toList)
      _.complete(result.status, List(`Content-Range`(ContentRange.Unsatisfiable(actualEntityLength))), result)

    case rejections @ (UnacceptedResponseContentTypeRejection(_) :: _) =>
      val supportedContentTypes = rejections.flatMap {
        case UnacceptedResponseContentTypeRejection(supported) => supported
        case _ => Nil
      }
      failed(NotAcceptable.unacceptedResponseContentType(supportedContentTypes))

    case rejections @ (UnacceptedResponseEncodingRejection(_) :: _) =>
      val supportedHttpEncoding = rejections.collect { case UnacceptedResponseEncodingRejection(supported) => supported }
      failed(NotAcceptable.unacceptedResponseEncoding(supportedHttpEncoding))

    case rejections @ (UnsupportedRequestContentTypeRejection(_) :: _) =>
      val supportedContentTypes = rejections.collect { case UnsupportedRequestContentTypeRejection(supported) => supported }
      failed(UnsupportedMediaType.unsupportedRequestContentType(supportedContentTypes))

    case rejections @ (UnsupportedRequestEncodingRejection(_) :: _) =>
      val supportedHttpEncodings = rejections.collect { case UnsupportedRequestEncodingRejection(supported) => supported }
      failed(BadRequest.unsupportedRequestEncoding(supportedHttpEncodings))

    case ValidationRejection(msg, _) :: _ => failed(BadRequest.validation(msg))
  }

}
