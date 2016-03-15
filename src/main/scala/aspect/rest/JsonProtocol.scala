package aspect.rest

import akka.http.scaladsl.model.StatusCode
import aspect.rest.Errors.ErrorResult
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol {

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    private lazy val format = ISODateTimeFormat.dateTimeNoMillis()

    def write(datetime: DateTime): JsValue = JsString(format.print(datetime.withZone(DateTimeZone.UTC)))

    def read(json: JsValue): DateTime = json match {
      case JsString(x) => format.parseDateTime(x)
      case x => deserializationError("Expected DateTime as JsString, but got " + x)
    }
  }

  implicit object StatusCodeJsonFormat extends RootJsonFormat[StatusCode] {
    def write(statusCode: StatusCode): JsValue = JsNumber(statusCode.intValue())

    def read(json: JsValue): StatusCode = json match {
      case JsNumber(x) => StatusCode.int2StatusCode(x.toInt)
      case x => deserializationError("Expected StatusCode as JsNumber, but got " + x)
    }
  }

  implicit val jsonError = jsonFormat3(ErrorResult)
}

