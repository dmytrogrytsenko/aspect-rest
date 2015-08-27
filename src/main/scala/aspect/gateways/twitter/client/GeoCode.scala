package aspect.gateways.twitter.client

import scala.util.Try

case class GeoCode(latitude: Double, longitude: Double, radius: Distance) {
  override def toString = s"$latitude,$longitude,$radius"
}

object GeoCode {
  private val pattern = "(.+),(.+),(.+)".r

  def apply(text: String): GeoCode = {
    val pattern(latitude, longitude, radius) = text
    GeoCode(latitude.toDouble, longitude.toDouble, radius.toDistance)
  }

  def tryToParse(text: String) = Try(apply(text)).toOption
}
