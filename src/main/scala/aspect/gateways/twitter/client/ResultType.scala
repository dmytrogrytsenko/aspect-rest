package aspect.gateways.twitter.client

sealed abstract class ResultType(val code: String)

object ResultTypes {
  case object popular extends ResultType("popular")
  case object mixed extends ResultType("mixed")
  case object recent extends ResultType("recent")
}

object ResultType {
  def apply(value: String): ResultType = {
    import ResultTypes._
    value match {
      case popular.code => popular
      case mixed.code => mixed
      case recent.code => recent
      case _ => throw new IllegalArgumentException("Invalid result type.")
    }
  }
}
