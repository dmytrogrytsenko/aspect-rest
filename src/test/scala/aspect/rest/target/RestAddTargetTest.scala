package aspect.rest.target

import aspect.controllers.target.{AddTargetResult, AddTargetData}
import aspect.common._
import aspect.TestBase
import aspect.domain.{ProjectId, Target}
import aspect.rest.Errors.{BadRequest, NotFound, Forbidden, Unauthorized}
import aspect.routes.TargetRoutesJson._

class RestAddTargetTest extends TestBase {

  behavior of "POST /targets"

  it should "create new target" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val data = AddTargetData(name = newUUID, projectId = project.id, keywords = newUUID)
    //act
    val result = Rest.addTarget(data, session.token).to[AddTargetResult]
    //assert
    val storedTarget = Mongo.getTarget(result.targetId)
    storedTarget should be (Some(Target(result.targetId, project.id, data.name, data.keywords)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(result.targetId)
  }

  it should "return 400 (Bad Request) VALIDATION if name is empty" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val data = AddTargetData(project.id, "", newUUID)
    //act
    val result = Rest.addTarget(data, session.token).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("name"))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //arrange
    val data = AddTargetData(ProjectId.generate(), newUUID, newUUID)
    //act
    val result = Rest.addTarget(data, null).toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //arrange
    val data = AddTargetData(ProjectId.generate(), newUUID, newUUID)
    //act
    val result = Rest.addTarget(data, newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val session = Mongo.addSession()
    val project = Mongo.addProject()
    val data = AddTargetData(project.id, newUUID, newUUID)
    //act
    val result = Rest.addTarget(data, session.token).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 401 (Unauthorized) ACCESS_DENIED if user is not owner of project" in {
    //arrange
    val owner = Mongo.addUser()
    val project = Mongo.addProject(userId = owner.id)
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val data = AddTargetData(project.id, newUUID, newUUID)
    //act
    val result = Rest.addTarget(data, session.token).toErrorResult
    //assert
    result should be (Forbidden.accessDenied)
    //cleanup
    Mongo.removeUser(owner.id)
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 404 (Not Found) PROJECT_NOT_FOUND if project not found" in {
    //arrange
    val projectId = ProjectId.generate()
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val data = AddTargetData(projectId, newUUID, newUUID)
    //act
    val result = Rest.addTarget(data, session.token).toErrorResult
    //assert
    result should be (NotFound.projectNotFound)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }
}
