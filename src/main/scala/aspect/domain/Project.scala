package aspect.domain

import aspect.common._

case class ProjectId(underlying: String) extends AnyVal

object ProjectId {
  def generate() = ProjectId(newUUID)
}

case class Project(id: ProjectId, userId: UserId, name: String)
