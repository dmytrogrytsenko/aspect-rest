package aspect.domain

case class KeywordId(underlying: String) extends AnyVal

case class Keyword(id: KeywordId, value: String, targets: Set[TargetId])
