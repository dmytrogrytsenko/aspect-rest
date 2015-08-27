package aspect.gateways.twitter.client

import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.joda.time.DateTime

case class SearchTweetsRequest(q: String,
                               geocode: Option[GeoCode] = None,
                               lang: Option[Language] = None,
                               locale: Option[Language] = None,
                               result_type: Option[ResultType] = None,
                               count: Option[Int] = None,
                               until: Option[DateTime] = None,
                               since_id: Option[Long] = None,
                               max_id: Option[Long] = None,
                               include_entities: Option[Boolean] = None,
                               callback: Option[String] = None)

case class SearchTweetsResponse(status: HttpResponseStatus,
                                rateLimitStatus: Option[RateLimitStatus],
                                result: Either[ErrorResult, SearchTweetsResult])

case class SearchTweetsResult(statuses: List[TwitterStatus])

case class TwitterStatus(id: Long, id_str: String, created_at: String, user: TwitterUser, text: String)

case class TwitterUser(id: Long, id_str: String, name: String, screen_name: String)
