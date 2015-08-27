package aspect.mongo

import aspect.common._
import aspect.TestBase
import aspect.domain.UserId

import scala.concurrent.ExecutionContext.Implicits.global

class ProjectCollectionTest extends TestBase {

  import ProjectCollection._

  behavior of "getUserProjects"

  it should "return user projects" in {
    //arrange
    val userId = UserId.generate()
    val project1 = Mongo.addProject(userId = userId)
    val project2 = Mongo.addProject(userId = userId)
    val project3 = Mongo.addProject()
    //act
    val result = getUserProjects(userId).await
    //assert
    result should contain.theSameElementsAs (List(project1, project2))
    //cleanup
    Mongo.removeProject(project1.id)
    Mongo.removeProject(project2.id)
    Mongo.removeProject(project3.id)
  }

  behavior of "update"

  it should "do nothing if no fileds defined" in {
    //arrange
    val project = Mongo.addProject(userId = UserId.generate())
    //act
    update(project.id, None).await
    //assert
    Mongo.getProject(project.id) should be (Some(project))
    //cleanup
    Mongo.removeProject(project.id)
  }

  it should "update name only" in {
    //arrange
    val project = Mongo.addProject()
    val newName = newUUID
    //act
    update(project.id, Some(newName)).await
    //assert
    Mongo.getProject(project.id) should be (Some(project.copy(name = newName)))
    //cleanup
    Mongo.removeProject(project.id)
  }
}
