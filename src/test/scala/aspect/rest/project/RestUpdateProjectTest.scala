package aspect.rest.project

import aspect.TestBase
import aspect.common._
import aspect.controllers.project.UpdateProjectData
import aspect.domain.{UserId, ProjectId}
import aspect.rest.Errors.{BadRequest, Forbidden, NotFound, Unauthorized}

class RestUpdateProjectTest extends TestBase {

  behavior of "PUT /projects/:id"

  it should "do nothing if no fileds defined" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val data = UpdateProjectData()
    //act
    Rest.updateProject(project.id, data, session.token).getStatusCode should be (200)
    //assert
    Mongo.getProject(project.id) should be (Some(project))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "update name only" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val data = UpdateProjectData(name = Some(newUUID))
    //act
    Rest.updateProject(project.id, data, session.token).getStatusCode should be (200)
    //assert
    Mongo.getProject(project.id) should be (Some(project.copy(name = data.name.get)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 400 (Bad Request) VALIDATION if name is empty" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val data = UpdateProjectData(Some(""))
    //act
    val result = Rest.updateProject(project.id, data, session.token).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("name"))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.updateProject(ProjectId.generate(), UpdateProjectData()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIAL_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.updateProject(ProjectId.generate(), UpdateProjectData(), newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIAL_REJECTED if user not found" in {
    //arrange
    val userId = UserId.generate()
    val session = Mongo.addSession(userId = userId)
    val project = Mongo.addProject(userId = userId)
    //act
    val result = Rest.updateProject(project.id, UpdateProjectData(), session.token).toErrorResult
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
    //act
    val result = Rest.updateProject(project.id, UpdateProjectData(), session.token).toErrorResult
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
    //act
    val result = Rest.updateProject(projectId, UpdateProjectData(), session.token).toErrorResult
    //assert
    result should be (NotFound.projectNotFound)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }
}
