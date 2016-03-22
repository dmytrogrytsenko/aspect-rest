package aspect

import aspect.common._
import org.joda.time.DateTime
import org.scalatest.Matchers

import scala.concurrent.duration.FiniteDuration

trait DateTimeRangeSupport extends Matchers {
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
