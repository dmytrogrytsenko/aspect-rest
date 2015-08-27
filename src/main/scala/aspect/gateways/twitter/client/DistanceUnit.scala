package aspect.gateways.twitter.client

sealed abstract class DistanceUnit(val code: String) {
  override def toString = code
}

object DistanceUnits {
  case object MILES extends DistanceUnit("mi")
  case object KILOMETERS extends DistanceUnit("km")
}

object DistanceUnit {
  def apply(value: String) = {
    import DistanceUnits._
    value match {
      case MILES.code => MILES
      case KILOMETERS.code => KILOMETERS
      case _ => throw new IllegalArgumentException("Invalid distance unit.")
    }
  }
}
