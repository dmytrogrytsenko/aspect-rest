package aspect.gateways.twitter.client

import scala.util.Try

case class Distance(value: Int, unit: DistanceUnit) {
  override def toString = s"$value$unit"
}

object Distance {
  private val pattern = "(\\d+)(\\w+)".r

  def apply(text: String): Distance = {
    val pattern(value, unit) = text
    Distance(value.toInt, unit.toDistanceUnit)
  }

  def tryToParse(text: String): Option[Distance] = Try(apply(text)).toOption
}
