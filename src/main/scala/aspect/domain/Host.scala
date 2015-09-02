package aspect.domain

case class HostId(underlying: String) extends AnyVal

case class Host(id: HostId, url: String)
