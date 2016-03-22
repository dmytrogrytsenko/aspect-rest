package aspect.mongo

import aspect.TestBase
import aspect.common._

import scala.concurrent.ExecutionContext.Implicits.global

class TwitterRequestCollectionTest extends TestBase {

  import TwitterRequestCollection._

  behavior of "Serializers"

  it should "read and write entity correctly" in {
    //arrange
    val request = buildTwitterRequest()
    //act
    add(request).await
    val storedRequest = get(request.id).await.get
    //assert
    storedRequest should be (request)
    //cleanup
    remove(request.id).await
  }

  behavior of "enabledRequests"

  it should "return not disabled request only" in {
    //arrange
    val request1 = buildTwitterRequest(disabled = None)
    val request2 = buildTwitterRequest(disabled = Some(true))
    val request3 = buildTwitterRequest(disabled = Some(false))
    Seq(request1, request2, request3).foreach(add(_).await)
    //act
    val result = enabledRequests.await
    //assert
    result should contain.allOf(request1, request3)
    result should not contain request2
    //cleanup
    remove(request1.id).await
    remove(request2.id).await
    remove(request3.id).await
  }
}
