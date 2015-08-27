package aspect.rest.user

import aspect.TestBase
import aspect.common._
import aspect.controllers.user.{LoginData, LoginResult}
import aspect.rest.Errors.{BadRequest, Unauthorized}
import aspect.routes.UserRoutesJson._
import org.joda.time.DateTime

class RestLoginTest extends TestBase {

  behavior of "POST /login"

  it should "create session and return token without case sensitive username" in {
    //arrange
    val user = Mongo.addUser(name = newUUID + "abcXYZ")
    //act
    val startTime = DateTime.now
    val result = Rest.login(LoginData(user.name.toUpperCase, user.password)).to[LoginResult]
    val endTime = DateTime.now
    //assert
    val session = Mongo.getSession(result.token).get
    session.token should be (result.token)
    session.userId should be (user.id)
    session.createdAt shouldBeInRange (startTime -> endTime)
    session.lastActivityAt shouldBeInRange (startTime -> endTime)
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(result.token)
  }

  it should "return 400 (Bad Request) VALIDATION if username is empty" in {
    //act
    val result = Rest.login(LoginData("", newUUID)).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("login"))
  }

  it should "return 400 (Bad Request) VALIDATION if password is empty" in {
    //act
    val result = Rest.login(LoginData(newUUID, "")).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("password"))
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val login = newUUID
    //act
    val result = Rest.login(LoginData(login, newUUID)).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if password is incorrected" in {
    //arrange
    val user = Mongo.addUser()
    //act
    val result = Rest.login(LoginData(user.name, newUUID)).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeUser(user.id)
  }
}
