package aspect.rest.project

import aspect.TestBase
import aspect.common._
import aspect.controllers.project.{AddProjectData, AddProjectResult}
import aspect.domain.{UserId, Project}
import aspect.rest.Errors.{BadRequest, Unauthorized}

class RestAddProjectTest extends TestBase {

  behavior of "POST /projects"

  it should "create new project" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val data = AddProjectData(name = newUUID)
    //act
    val result = Rest.addProject(data, session.token).to[AddProjectResult]
    //assert
    val storedProject = Mongo.getProject(result.projectId)
    storedProject should be (Some(Project(result.projectId, user.id, data.name)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(result.projectId)
  }

  it should "return 400 (Bad Request) VALIDATION if name is empty" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.addProject(AddProjectData(""), session.token).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("name"))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.addProject(AddProjectData(newUUID)).toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.addProject(AddProjectData(newUUID), newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val userId = UserId.generate()
    val session = Mongo.addSession(userId = userId)
    val data = AddProjectData(name = newUUID)
    //act
    val result = Rest.addProject(data, session.token).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeSession(session.token)
  }

}
