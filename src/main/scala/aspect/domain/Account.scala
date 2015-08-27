package aspect.domain

case class AccountHost(id: String)

case class Account(id: String, url: String, host: AccountHost, name: String)
