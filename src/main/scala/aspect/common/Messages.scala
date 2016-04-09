package aspect.common

object Messages {
  case object Start
  trait Tick
  case object Tick extends Tick
  trait Done
  case object Done extends Done
}
