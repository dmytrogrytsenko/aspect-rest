package aspect.rest.project

import aspect.TestBase
import aspect.common._
import aspect.controllers.project.{ProjectItemResult, ProjectListResult}
import aspect.rest.Errors.Unauthorized

class RestGetProjectsTest extends TestBase {

  behavior of "GET /projects"

  it should "return user projects successfully" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project1 = Mongo.addProject(userId = user.id)
    val project2 = Mongo.addProject(userId = user.id)
    //act
    val result = Rest.getProjects(session.token).to[ProjectListResult]
    //assert
    result.projects should contain.theSameElementsAs(List(ProjectItemResult(project1), ProjectItemResult(project2)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project1.id)
    Mongo.removeProject(project2.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.getProjects().toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.getProjects(newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val session = Mongo.addSession()
    //act
    val result = Rest.getProjects(session.token).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeSession(session.token)
  }
}
