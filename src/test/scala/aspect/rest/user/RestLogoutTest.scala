package aspect.rest.user

import aspect.TestBase
import aspect.common._
import aspect.rest.Errors.Unauthorized

class RestLogoutTest extends TestBase {

  behavior of "POST /logout"

  it should "delete session" in {
    //arrange
    val session = Mongo.addSession()
    //act
    Rest.logout(session.token).getStatusCode should be (200)
    //assert
    Mongo.getSession(session.token) should be (None)
  }

  it should "do nothing if session not found" in {
    //act
    Rest.logout(newUUID).getStatusCode should be (200)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_MISSING if authorization token is not defined" in {
    //act
    val result = Rest.logout().toErrorResult
    //assert
    result should be (Unauthorized.credentialsMissing)
  }
}
