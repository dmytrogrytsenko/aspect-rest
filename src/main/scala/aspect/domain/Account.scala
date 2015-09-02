package aspect.domain

case class AccountId(underlying: String) extends AnyVal

case class AccountHost(id: HostId)

case class Account(id: AccountId, url: String, host: AccountHost, name: String)
