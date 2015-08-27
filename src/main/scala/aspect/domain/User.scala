package aspect.domain

import aspect.common._

case class UserId(underlying: String) extends AnyVal

object UserId {
  def generate() = UserId(newUUID)
}

case class User(id: UserId,
                name: String,
                password: String,
                email: String,
                firstName: Option[String],
                lastName: Option[String],
                company: Option[String])
