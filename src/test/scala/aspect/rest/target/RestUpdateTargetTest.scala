package aspect.rest.target

import aspect.TestBase
import aspect.common._
import aspect.controllers.target.UpdateTargetData
import aspect.domain.{ProjectId, UserId, TargetId}
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}

class RestUpdateTargetTest extends TestBase {

  behavior of "PUT /targets/:id"

  it should "do nothing if no fields defined" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val target = Mongo.addTarget(projectId = project.id)
    //act
    val data = UpdateTargetData()
    Rest.updateTarget(target.id, data, session.token).getStatusCode should be (200)
    //assert
    Mongo.getTarget(target.id) should be (Some(target))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target.id)
  }

  it should "update name only" in {
    //arrange
    val newName = newUUID
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val target = Mongo.addTarget(projectId = project.id)
    //act
    val data = UpdateTargetData(name = Some(newName))
    Rest.updateTarget(target.id, data, session.token).getStatusCode should be (200)
    //assert
    Mongo.getTarget(target.id) should be (Some(target.copy(name = newName)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target.id)
  }

  it should "update kwywords only" in {
    //arrange
    val newKeywords = newUUID
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val target = Mongo.addTarget(projectId = project.id)
    //act
    val data = UpdateTargetData(keywords = Some(newKeywords))
    Rest.updateTarget(target.id, data, session.token).getStatusCode should be (200)
    //assert
    Mongo.getTarget(target.id) should be (Some(target.copy(keywords = newKeywords)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target.id)
  }

  it should "update all fields" in {
    //arrange
    val newName = newUUID
    val newKeywords = newUUID
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val target = Mongo.addTarget(projectId = project.id)
    //act
    val data = UpdateTargetData(name = Some(newName), keywords = Some(newKeywords))
    Rest.updateTarget(target.id, data, session.token).getStatusCode should be (200)
    //assert
    Mongo.getTarget(target.id) should be (Some(target.copy(name = newName, keywords = newKeywords)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.updateTarget(TargetId.generate(), UpdateTargetData(), token = null).toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.updateTarget(TargetId.generate(), UpdateTargetData(), token = newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val userId = UserId.generate()
    val session = Mongo.addSession(userId = userId)
    val project = Mongo.addProject(userId = userId)
    val target = Mongo.addTarget(projectId = project.id)
    //act
    val result = Rest.updateTarget(target.id, UpdateTargetData(), session.token).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target.id)
  }

  it should "return 404 (Not Found) TARGET_NOT_FOUND if target not found" in {
    //arrange
    val targetId = TargetId.generate()
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.updateTarget(targetId, UpdateTargetData(), session.token).toErrorResult
    //assert
    result should be (NotFound.targetNotFound)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }

  it should "return 404 (Not Found) PROJECT_NOT_FOUND if project not found" in {
    //arrange
    val projectId = ProjectId.generate()
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val target = Mongo.addTarget(projectId = projectId)
    //act
    val result = Rest.updateTarget(target.id, UpdateTargetData(), session.token).toErrorResult
    //assert
    result should be (NotFound.projectNotFound)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeTarget(target.id)
  }

  it should "return 401 (Unauthorized) ACCESS_DENIED if user is not owner of project" in {
    //arrange
    val owner = Mongo.addUser()
    val project = Mongo.addProject(userId = owner.id)
    val target = Mongo.addTarget(projectId = project.id)
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.updateTarget(target.id, UpdateTargetData(), session.token).toErrorResult
    //assert
    result should be (Forbidden.accessDenied)
    //cleanup
    Mongo.removeUser(owner.id)
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target.id)
  }
}
