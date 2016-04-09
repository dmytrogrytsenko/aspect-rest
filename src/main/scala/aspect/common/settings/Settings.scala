package aspect.common.settings

trait Settings {
  def get[T](name: String): T
}