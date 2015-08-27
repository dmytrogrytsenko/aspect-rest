package aspect.domain

import aspect.common._

case class TargetId(underlying: String) extends AnyVal

object TargetId {
  def generate() = TargetId(newUUID)
}

case class Target(id: TargetId, projectId: ProjectId, name: String, keywords: String) {
  lazy val words = keywords.split("//W").map(_.trim.toLowerCase).filter(_.nonEmpty).toSet
}
