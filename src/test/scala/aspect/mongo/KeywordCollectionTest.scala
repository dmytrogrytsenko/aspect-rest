package aspect.mongo

import aspect.common._
import aspect.TestBase
import reactivemongo.api.indexes.{IndexType, Index}

import scala.concurrent.ExecutionContext.Implicits.global

class KeywordCollectionTest extends TestBase {

  import KeywordCollection._

  behavior of "ensureIndexes"

  it should "ensure indexes" in {
    //arrange
    //dropIndexes
    //act
    //ensureIndexes.await
    //assert
    val indexes = items.indexesManager.list().await
    indexes should contain (Index(
      key = Seq("value" -> IndexType.Ascending),
      name = Some("value_1"),
      unique = true,
      version = Some(1)))
    indexes should contain (Index(
      key = Seq("targets" -> IndexType.Ascending),
      name = Some("targets_1"),
      version = Some(1)))
  }
}
