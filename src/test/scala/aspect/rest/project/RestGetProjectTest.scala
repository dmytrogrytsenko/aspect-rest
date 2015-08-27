package aspect.rest.project

import aspect.TestBase
import aspect.common._
import aspect.controllers.project.{ProjectResult, ProjectUserResult}
import aspect.domain.{ProjectId, UserId}
import aspect.rest.Errors.{Forbidden, NotFound, Unauthorized}
import aspect.routes.ProjectRoutesJson._

class RestGetProjectTest extends TestBase {

  behavior of "GET /projects/:id"

  it should "return project successfully" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    //act
    val result = Rest.getProject(project.id, session.token).to[ProjectResult]
    //assert
    result should be(ProjectResult(project.id, project.name, ProjectUserResult(user.id, user.name)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.getProject(ProjectId.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.getProject(ProjectId.generate(), token = newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val userId = UserId.generate()
    val session = Mongo.addSession(userId = userId)
    val project = Mongo.addProject(userId = userId)
    //act
    val result = Rest.getProject(project.id, session.token).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }

  it should "return 404 (Not Found) PROJECT_NOT_FOUND if project not found" in {
    //arrange
    val projectId = ProjectId.generate()
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getProject(projectId, session.token).toErrorResult
    //assert
    result should be (NotFound.projectNotFound)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }

  it should "return 401 (Unauthorized) ACCESS_DENIED if user is not owner of project" in {
    //arrange
    val owner = Mongo.addUser()
    val project = Mongo.addProject(userId = owner.id)
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getProject(project.id, session.token).toErrorResult
    //assert
    result should be (Forbidden.accessDenied)
    //cleanup
    Mongo.removeUser(owner.id)
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
  }
}
