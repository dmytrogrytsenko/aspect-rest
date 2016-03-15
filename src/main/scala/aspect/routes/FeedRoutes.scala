package aspect.routes

import aspect.common._
import aspect.controllers.feed.{PostAuthorResult, PostResult, FeedResult, GetFeedController}
import aspect.domain.{PostId, AccountId}
import aspect.rest.{JsonProtocol, Routes}
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.json.{JsonFormat, JsValue, JsString, DeserializationException}

trait FeedRoutesJson extends JsonProtocol {
  implicit object AccountIdJsonFormat extends JsonFormat[AccountId] {
    def read(json: JsValue): AccountId = json match {
      case JsString(value) => AccountId(value)
      case _ => throw new DeserializationException("Expected AccountId as JsString")
    }
    def write(value: AccountId): JsValue = JsString(value.underlying)
  }

  implicit object PostIdJsonFormat extends JsonFormat[PostId] {
    def read(json: JsValue): PostId = json match {
      case JsString(value) => PostId(value)
      case _ => throw new DeserializationException("Expected PostId as JsString")
    }
    def write(value: PostId): JsValue = JsString(value.underlying)
  }

  implicit val jsonPostAuthorResult = jsonFormat3(PostAuthorResult.apply)
  implicit val jsonPostResult = jsonFormat6(PostResult.apply)
  implicit val jsonFeedResult = jsonFormat1(FeedResult.apply)
}

trait FeedRoutes extends Routes with FeedRoutesJson {

  import context.dispatcher

  val getFeedRoute =
    path("feed") {
      get {
        complete {
          GetFeedController.props.execute[FeedResult]
        }
      }
    }

  val feedRoutes = getFeedRoute
}
