package aspect

import akka.util.Timeout
import aspect.common._
import org.joda.time.DateTime
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

trait TestBase extends FlatSpecLike with Matchers with MongoSupport with RestSupport {
  implicit val timeout: Timeout = Timeout(5.seconds)

  implicit class DateTimeRangeBuilder(instance: DateTime) {
    def +-(tolerance: FiniteDuration) = (instance - tolerance) -> (instance + tolerance)
  }

  implicit class DateTimeRangeMatcher(instance: DateTime) {
    def shouldBeInRange(range: (DateTime, DateTime)) = {
      instance should be >= range._1
      instance should be <= range._2
    }
  }
}
