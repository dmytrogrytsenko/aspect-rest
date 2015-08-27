package aspect.gateways.twitter.client

import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {
  implicit val jsonError = jsonFormat2(Error)
  implicit val jsonErrorResult = jsonFormat1(ErrorResult)
  implicit val jsonTwitterUser = jsonFormat4(TwitterUser)
  implicit val jsonTwitterStatus = jsonFormat5(TwitterStatus)
  implicit val jsonSearchTweetsResult = jsonFormat1(SearchTweetsResult)
}
