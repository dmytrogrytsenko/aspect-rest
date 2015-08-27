package aspect.mongo

import aspect.common._
import aspect.TestBase
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

class SessionCollectionTest extends TestBase {

  import SessionCollection._

  behavior of "activity"

  it should "update lastActivityAt with DateTime.now" in {
    //arrange
    val session = Mongo.addSession(lastActivityAt = DateTime.now.minusMinutes(5))
    //act
    val startTime = DateTime.now
    activity(session.token).await
    val endTime = DateTime.now
    //assert
    Mongo.getSession(session.token).get.lastActivityAt shouldBeInRange (startTime -> endTime)
    //cleanup
    Mongo.removeSession(session.token)
  }

  it should "do nothing if no token found" in {
    //act
    activity(newUUID).await
  }
}
