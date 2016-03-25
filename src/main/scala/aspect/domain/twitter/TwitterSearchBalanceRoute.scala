package aspect.domain.twitter

sealed trait TwitterSearchBalanceRoute

object TwitterSearchBalanceRoute {
  case object Adaptive extends TwitterSearchBalanceRoute
  case object Oldest extends TwitterSearchBalanceRoute
  case object Backward extends TwitterSearchBalanceRoute
  val all = List(Adaptive, Oldest, Backward)
}
