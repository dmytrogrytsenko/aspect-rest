package aspect

import akka.util.Timeout
import org.scalatest.FlatSpecLike

import scala.concurrent.duration._

trait TestBase extends FlatSpecLike
  with DateTimeRangeSupport
  with MongoSupport
  with RestSupport
  with EntityBuilders {

  implicit val timeout: Timeout = Timeout(5.seconds)
}
