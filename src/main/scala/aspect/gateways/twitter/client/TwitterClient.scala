package aspect.gateways.twitter.client

import com.ning.http.client.Response
import dispatch.{url, Http}
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object TwitterClient {
  def apply(data: TwitterAuthData) = new TwitterClient(data)
}

class TwitterClient(data: TwitterAuthData) {

  def searchTweets(request: SearchTweetsRequest)
                  (implicit executionContext: ExecutionContext): Future[SearchTweetsResponse] = {
    val searchTweetsUrl = "https://api.twitter.com/1.1/search/tweets.json"
    val parameters = getSearchTweetsParameters(request)
    val authorizationHeader = getAuthorizationHeader(searchTweetsUrl, parameters)
    val req = url(searchTweetsUrl).GET
      .setHeader("Authorization", authorizationHeader)
      .setQueryParameters(parameters.toList.map(x => x._1 -> Seq(x._2)).toMap)
    Http(req) map { response =>
      val status = valueOf(response.getStatusCode)
      val body = response.getResponseBody.parseJson
      val rateLimitStatus = extractRateLimitStatus(response)
      import JsonProtocol._
      SearchTweetsResponse(status, rateLimitStatus, status match {
        case OK => Right(body.convertTo[SearchTweetsResult])
        case _ => Left(body.convertTo[ErrorResult])
      })
    }
  }

  def getSearchTweetsParameters(request: SearchTweetsRequest) = List(
    "q" -> Some(request.q),
    "geocode" -> request.geocode.map(_.toString),
    "lang" -> request.lang,
    "locale" -> request.locale,
    "result_type" -> request.result_type.map(_.code),
    "count" -> request.count.map(_.toString),
    "until" -> request.until.map(_.toString("yyyy-MM-dd")),
    "since_id" -> request.since_id.map(_.toString),
    "max_id" -> request.max_id.map(_.toString),
    "include_entities" -> request.include_entities.map(_.toString),
    "callback" -> request.callback)
    .flatMap(x => x._2.map(x._1 -> _)).toMap

  def getAuthorizationHeader(url: String, parameters: Map[String, String]): String = {
    import twitter4j.auth.OAuthAuthorization
    import twitter4j.conf.ConfigurationBuilder
    import twitter4j.{HttpParameter, HttpRequest, RequestMethod}
    val twitterConfig = new ConfigurationBuilder()
      .setOAuthConsumerKey(data.consumerKey)
      .setOAuthConsumerSecret(data.consumerSecret)
      .setOAuthAccessToken(data.accessToken)
      .setOAuthAccessTokenSecret(data.accessTokenSecret)
      .build
    val authorization = new OAuthAuthorization(twitterConfig)
    val httpRequest = new HttpRequest(
      RequestMethod.GET,
      url,
      parameters.toList.map(x => new HttpParameter(x._1, x._2)).toArray,
      authorization,
      null)
    authorization.getAuthorizationHeader(httpRequest)
  }

  def extractRateLimitStatus(response: Response) = Try(RateLimitStatus(
    remaining = response.getHeader("X-Rate-Limit-Remaining").toInt,
    limit = response.getHeader("X-Rate-Limit-Limit").toInt,
    reset = response.getHeader("X-Rate-Limit-Reset").toInt)).toOption
}
