package controllers

import play.api.Logger
import play.api.mvc.{Action, Controller}

object CORSController extends Controller {

  def preFlight(all: String) = Action {
    Logger.info("setting responses for /OPTIONS request")
    Ok("").withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent");
  }

}
