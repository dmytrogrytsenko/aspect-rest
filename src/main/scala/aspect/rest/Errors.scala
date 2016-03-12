package aspect.rest

import akka.http.scaladsl.model.headers.{ByteRange, HttpEncoding}
import akka.http.scaladsl.model.{ContentType, HttpMethod, StatusCodes, StatusCode}

object Errors {

  type ErrorCode = String

  case class ErrorResult(status: StatusCode, code: ErrorCode, message: String)

  class RestException(val result: ErrorResult)
    extends RuntimeException(s"${result.status.intValue} (${result.status.reason}}) ${result.code} ${result.message}")

  //400
  object BadRequest {
//    def corruptRequestEncoding(message: String) = ErrorResult(StatusCodes.BadRequest, "CORRUPT_REQUEST_ENCODING", s"The request's encoding is corrupt:\n$message")
//    def malformedFormField(name: String, message: String) = ErrorResult(StatusCodes.BadRequest, "MALFORMED_FORM_FIELD", s"The form field '$name' was malformed:\n$message")
//    def malformedHeader(headerName: String, message: String) = ErrorResult(StatusCodes.BadRequest, "MALFORMED_HEADER", s"The value of HTTP header '$headerName' was malformed:\n$message")
//    def malformedQueryParam(name: String, message: String) = ErrorResult(StatusCodes.BadRequest, "MALFORMED_QUERY_PARAM", s"The query parameter '$name' was malformed:\n$message")
//    def malformedRequestContent(message: String) = ErrorResult(StatusCodes.BadRequest, "MALFORMED_REQUEST_CONTENT", s"The request content was malformed:\n$message")
//    def missingCookie(cookieName: String) = ErrorResult(StatusCodes.BadRequest, "MISSING_COOKIE", s"Request is missing required cookie '$cookieName'.")
//    def missingFormField(fieldName: String) = ErrorResult(StatusCodes.BadRequest, "MISSING_FORM_FIELD", s"Request is missing required form field '$fieldName'.")
//    def missingHeader(headerName: String) = ErrorResult(StatusCodes.BadRequest, "MISSING_HEADER", s"Request is missing required HTTP header '$headerName'.")
//    def missingQueryParam(paramName: String) = ErrorResult(StatusCodes.BadRequest, "MISSING_QUERY_PARAM", s"Request is missing required query parameter '$paramName'.")
//    def requestEntityExpected = ErrorResult(StatusCodes.BadRequest, "REQUEST_ENTITY_EXPECTED", "Request entity expected but not supplied.")
//    def schemeNotAllowed(supportedSchemes: List[String]) = ErrorResult(StatusCodes.BadRequest, "SCHEME_NOT_ALLOWED", s"Uri scheme not allowed, supported schemes: ${supportedSchemes.mkString(", ")}.")
    def validation(message: String) = ErrorResult(StatusCodes.BadRequest, "VALIDATION", message)
    object Validation {
      def requiredMemberEmpty(name: String) = validation(s"The request content validation is failed: Required member '$name' is empty")
    }
//    def unsupportedRequestEncoding(supportedHttpEncodings: List[HttpEncoding]) = ErrorResult(StatusCodes.BadRequest, "UNSUPPORTED_REQUEST_ENCODING", "The request's Content-Encoding must be one the following:\n" + supportedHttpEncodings.map(_.value).mkString("\n"))
  }

  //401
  object Unauthorized {
    def credentialsMissing = ErrorResult(StatusCodes.Unauthorized, "CREDENTIALS_MISSING", "The resource requires authentication, which was not supplied with the request")
    def credentialsRejected = ErrorResult(StatusCodes.Unauthorized, "CREDENTIALS_REJECTED", "The supplied authentication is invalid")
  }

  //403
  object Forbidden {
    def accessDenied = ErrorResult(StatusCodes.Forbidden, "ACCESS_DENIED","The supplied authentication is not authorized to access this resource")
  }

  //404
  object NotFound {
    def projectNotFound = ErrorResult(StatusCodes.NotFound, "PROJECT_NOT_FOUND", "Project is not found")
//    def resourceNotFound = ErrorResult(StatusCodes.NotFound, "RESOURCE_NOT_FOUND", "The requested resource could not be found.")
    def targetNotFound = ErrorResult(StatusCodes.NotFound, "TARGET_NOT_FOUND", "Target is not found.")
  }

  //405
//  object MethodNotAllowed {
//    def default(methods: List[HttpMethod]) = ErrorResult(StatusCodes.MethodNotAllowed, "METHOD_NOT_ALLOWED", s"HTTP method not allowed, supported methods: ${methods.mkString(", ")}.")
//  }

  //406
//  object NotAcceptable {
//    def unacceptedResponseContentType(supportedContentTypes: List[ContentType]) = ErrorResult(StatusCodes.NotAcceptable, "UNACCEPTED_RESPONSE_CONTENT_TYPE", "Resource representation is only available with these Content-Types:\n" + supportedContentTypes.map(_.value).mkString("\n"))
//    def unacceptedResponseEncoding(supportedHttpEncodings: List[HttpEncoding]) = ErrorResult(StatusCodes.NotAcceptable, "UNACCEPTED_RESPONSE_ENCODING", "Resource representation is only available with these Content-Encodings:\n" + supportedHttpEncodings.map(_.value).mkString("\n"))
//  }

  //415
//  object UnsupportedMediaType {
//    def unsupportedRequestContentType(supportedContentTypes: List[String]) = ErrorResult(StatusCodes.UnsupportedMediaType, "UNSUPPORTED_REQUEST_CONTENT_TYPE", s"There was a problem with the request's Content-Type:\n${supportedContentTypes.mkString(" or ")}.")
//  }

  //416
//  object RequestedRangeNotSatisfiable {
//    def tooManyRanges = ErrorResult(StatusCodes.RequestedRangeNotSatisfiable, "TOO_MANY_RANGES", "Request contains too many ranges.")
//    def unsatisfiableRange(unsatisfiableRanges: List[ByteRange]) = ErrorResult(StatusCodes.RequestedRangeNotSatisfiable, "UNSATISFIABLE_RANGE", unsatisfiableRanges.mkString("None of the following requested ranges were satisfiable:\n", "\n", "."))
//  }

  //500
  object InternalServerError {
    def default = ErrorResult(StatusCodes.InternalServerError, "INTERNAL_SERVER_ERROR", "There was an internal server error.")
//    def timeout = ErrorResult(StatusCodes.InternalServerError, "TIMEOUT", "The request processing is timed out.")
  }
}
