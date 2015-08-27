package aspect.domain

import org.joda.time.DateTime

case class Session(token: String,
                   userId: UserId,
                   createdAt: DateTime,
                   lastActivityAt: DateTime)