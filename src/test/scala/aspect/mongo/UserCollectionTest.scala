package aspect.mongo

import aspect.common._
import aspect.TestBase
import reactivemongo.api.indexes.{Index, IndexType}

import scala.concurrent.ExecutionContext.Implicits.global

class UserCollectionTest extends TestBase {

  import UserCollection._

  behavior of "ensureIndexes"

  it should "ensure indexes" in {
    //arrange
    //dropIndexes
    //act
    //ensureIndexes.await
    //assert
    val indexes = items.indexesManager.list().await
    indexes should contain (Index(Seq("nameLC" -> IndexType.Ascending), name = Some("nameLC_1"), unique = true, version = Some(1)))
  }

  behavior of "findUserByName"

  it should "return user by name" in {
    //arrange
    val user = Mongo.addUser()
    //act
    val result = findUserByName(user.name).await
    //assert
    result should be (Some(user))
    //cleanup
    Mongo.removeUser(user.id)
  }

  it should "return user by name and ignore case" in {
    //arrange
    val user = Mongo.addUser(name = newUUID + "ABC")
    //act
    val result = findUserByName(user.name.toUpperCase).await
    //assert
    result should be (Some(user))
    //cleanup
    Mongo.removeUser(user.id)
  }

  it should "return None if user not found by name" in {
    //act
    val result = findUserByName(newUUID).await
    //assert
    result should be (None)
  }

}
