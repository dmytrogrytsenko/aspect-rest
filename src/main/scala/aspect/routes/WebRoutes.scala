package aspect.routes

import aspect.rest.Routes

trait WebRoutes extends Routes {

  val webRoutes =
    get {
      path("") {
        pathEndOrSingleSlash {
          getFromResource("web/index.html")
        }
      }
    } ~
      get {
        getFromResourceDirectory("web")
      }
}
