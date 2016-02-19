package aspect.common

object Messages {
  case object Start
  case object Stop
  trait Done
  case object Done extends Done
}
