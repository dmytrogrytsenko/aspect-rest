package aspect.gateways.twitter

import aspect.gateways.twitter.client.DistanceUnits.{KILOMETERS, MILES}
import org.joda.time.DateTime

package object client {

  type Language = String

  val MAX_SEARCH_TWEET_COUNT = 100

  implicit final class DistanceInt(val value: Int) extends AnyVal {
    def mi = Distance(value, MILES)
    def mile = mi
    def miles = mi
    def km = Distance(value, KILOMETERS)
    def kilometer = km
    def kilometers = km
  }

  implicit final class DistanceString(val value: String) extends AnyVal {
    def toDistance = Distance(value)
    def toDistanceUnit = DistanceUnit(value)
  }

  case class Error(code: Int, message: String)
  case class ErrorResult(errors: List[Error])
  case class RateLimitStatus(remaining: Int, limit: Int, reset: Int)
}
