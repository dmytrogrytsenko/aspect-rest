package aspect.rest.target

import aspect.TestBase
import aspect.controllers.target.{TargetItemResult, TargetListResult}
import aspect.common._
import aspect.domain.ProjectId
import aspect.rest.Errors.{NotFound, Forbidden, Unauthorized}
import aspect.routes.TargetRoutesJson._

class RestGetTargetsTest extends TestBase {

  behavior of "GET /projects/:projectId/targets"

  it should "return project targets successfully" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val project = Mongo.addProject(userId = user.id)
    val target1 = Mongo.addTarget(projectId = project.id)
    val target2 = Mongo.addTarget(projectId = project.id)
    //act
    val result = Rest.getTargets(project.id, session.token).to[TargetListResult]
    //assert
    result.targets should contain.theSameElementsAs(List(TargetItemResult(target1), TargetItemResult(target2)))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
    Mongo.removeProject(project.id)
    Mongo.removeTarget(target1.id)
    Mongo.removeTarget(target2.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.getTargets(ProjectId.generate(), null).toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.getTargets(ProjectId.generate(), newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val session = Mongo.addSession()
    val project = Mongo.addProject()
    //act
    val result = Rest.getTargets(project.id, session.token).toErrorResult
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
    val result = Rest.getTargets(project.id, session.token).toErrorResult
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
    val result = Rest.getTargets(projectId, session.token).toErrorResult
    //assert
    result should be (NotFound.projectNotFound)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }
}
