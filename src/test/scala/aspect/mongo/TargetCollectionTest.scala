package aspect.mongo

import aspect.common._
import aspect.TestBase
import aspect.domain.ProjectId

import scala.concurrent.ExecutionContext.Implicits.global

class TargetCollectionTest extends TestBase {

  import TargetCollection._

  behavior of "getProjectTargets"

  it should "return project targets" in {
    //arrange
    val projectId = ProjectId.generate()
    val target1 = Mongo.addTarget(projectId = projectId)
    val target2 = Mongo.addTarget(projectId = projectId)
    val target3 = Mongo.addTarget()
    //act
    val result = getProjectTargets(projectId).await
    //assert
    result should contain.theSameElementsAs (List(target1, target2))
    //cleanup
    Mongo.removeTarget(target1.id)
    Mongo.removeTarget(target2.id)
    Mongo.removeTarget(target3.id)
  }

  behavior of "update"

  it should "do nothing if no fields defined" in {
    //arrange
    val target = Mongo.addTarget()
    //act
    update(target.id, None, None).await
    //assert
    Mongo.getTarget(target.id) should be (Some(target))
  }

  it should "update name only" in {
    //arrange
    val newName = newUUID
    val target = Mongo.addTarget()
    //act
    update(target.id, name = Some(newName)).await
    //assert
    Mongo.getTarget(target.id) should be (Some(target.copy(name = newName)))
  }

  it should "update keywords only" in {
    //arrange
    val newKeywords = newUUID
    val target = Mongo.addTarget()
    //act
    update(target.id, keywords = Some(newKeywords)).await
    //assert
    Mongo.getTarget(target.id) should be (Some(target.copy(keywords = newKeywords)))
  }

  it should "update all fields" in {
    //arrange
    val newName = newUUID
    val newKeywords = newUUID
    val target = Mongo.addTarget()
    //act
    update(target.id, Some(newName), Some(newKeywords)).await
    //assert
    Mongo.getTarget(target.id) should be (Some(target.copy(name = newName, keywords = newKeywords)))
  }

}
