package aspect.rest.user

import aspect.TestBase
import aspect.common._
import aspect.controllers.user.ProfileResult
import aspect.rest.Errors.Unauthorized
import aspect.routes.UserRoutesJson._

class RestGetProfileTest extends TestBase {

  behavior of "GET /profile"

  it should "return user profile successfully" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getProfile(session.token).to[ProfileResult]
    //assert
    result should be (ProfileResult(
      id = user.id,
      name = user.name,
      email = user.email,
      firstName = user.firstName,
      lastName = user.lastName,
      company = user.company))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.getProfile().toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if authorization token is not correct" in {
    //act
    val result = Rest.getProfile(newUUID).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val session = Mongo.addSession()
    //act
    val result = Rest.getProfile(session.token).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeSession(session.token)
  }

}
