package aspect.mongo

import aspect.TestBase
import aspect.common._

import scala.concurrent.ExecutionContext.Implicits.global

class TwitterQueryCollectionTest extends TestBase {

  import TwitterQueryCollection._

  behavior of "Serializers"

  it should "read and write entity correctly" in {
    //arrange
    val query = buildTwitterQuery()
    //act
    add(query).await
    val storedQuery = get(query.id).await.get
    //assert
    storedQuery should be (query)
    //cleanup
    remove(query.id).await
  }

  behavior of "enabledQueries"

  it should "return not disabled request only" in {
    //arrange
    val query1 = buildTwitterQuery(disabled = None)
    val query2 = buildTwitterQuery(disabled = Some(true))
    val query3 = buildTwitterQuery(disabled = Some(false))
    Seq(query1, query2, query3).foreach(add(_).await)
    //act
    val result = enabledQueries.await
    //assert
    result should contain.allOf(query1, query3)
    result should not contain query2
    //cleanup
    remove(query1.id).await
    remove(query2.id).await
    remove(query3.id).await
  }
}
