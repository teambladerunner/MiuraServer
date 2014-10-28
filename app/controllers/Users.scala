package controllers

import akka.actor.{ActorRef, Props}
import model.dataobjects.UserDetail
import model.user.{NewUserRq, UserDBModel, UserStateActor}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.libs.Akka
import play.mvc.Http.HeaderNames

import scala.concurrent.Future

object Users extends Controller {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def getUserPersonalInfo(userId: String) = Action.async {
    Future {
      //TODO read from model
      Ok(Json.obj("status" -> "OK", "message" -> ("Hello " + userId + " , you're 43")))
    }
  }

  def getUserPortfolio(userId: String) = Action.async {
    Future {
      Ok(new UserDBModel().getUserStocksAsJson(userId))
    }
  }

  def getUserTradeHistory(userId: String) = Action.async {
    Future {
      Ok(new UserDBModel().getUserTradeHistoryAsJson(userId))
    }
  }

  val userStateActor: ActorRef = Akka.system.actorOf(Props.create(classOf[UserStateActor]), "userStateActor")

  def newUser = Action.async(parse.json) { request =>
    Future {
      Logger.info(request.body.toString())
      try {
        val userDetail = UserDetail.buildUserDetailFromJSON(request.body.toString())
        userStateActor ! new NewUserRq(userDetail)
        Ok("[]").withHeaders(
          HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
        )
      } catch {
        case exception: Exception => BadRequest("Missing parameter [name]")
      }
    }
  }

}
