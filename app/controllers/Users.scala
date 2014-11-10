package controllers

import akka.pattern.ask
import akka.util.Timeout
import controllers.Global._
import model.SystemSupervisor
import model.dataobjects.{Trade, UserDetail}
import model.stocks.TradeStock
import model.user._
import model.user.hash.{DCrypt, NCrypt}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContentAsJson}
import play.mvc.Http.HeaderNames

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

//class Users(override implicit val env: RuntimeEnvironment[DemoUser]) extends securesocial.core.SecureSocial[DemoUser] {
object Users extends play.api.mvc.Controller {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def loginMiuraUser = Action.async { request =>
    Future {
      Logger.info(request.body.toString())
      val content = request.body
      var requestBody = ""
      content match {
        case AnyContentAsJson(_) => requestBody = Json.stringify(content.asJson.get)
        case _ => requestBody = content.toString
      }
      Logger.info(requestBody)
      val userDetail = UserDetail.buildUserDetailFromJSON(requestBody)
      //hash the email and check
      val userDBModel = new UserDBModel().view.selectUserByEmail(userDetail.getEmail)
      if (userDBModel != null && new DCrypt().validatePassword(userDetail.getPassword, userDBModel.getPassword)) {
        Ok(JsObject(Seq("authid" -> JsString(userDBModel.getEmail + "535510N" + new NCrypt().hash(userDBModel.getEmail)))))
      } else {
        BadRequest(jsonify("invalid user id"))
      }
    }
  }

  def getUserPersonalInfo = Action.async { request =>
    Future {
      val userId = getUser(request)
      if (userId.length > 0) {
        Ok(new UserDBModel().view.selectUserByEmail(userId).toKVJSON)
      } else {
        BadRequest(jsonify("invalid user id"))
      }
    }
  }

  def getUserPortfolio = Action.async { request =>
    val futurePortfolio = Future {
      val userId = getUser(request)
      Logger.info("return portfolio for " + userId)
      new UserPortfolio(userId).asJSON
    }
    val futurePortfolioTimeout = play.api.libs.concurrent.Promise.timeout("Oops", 30.second)
    Future.firstCompletedOf(Seq(futurePortfolio, futurePortfolioTimeout)).map {
      case s: String => Ok(s)
      case _ => BadRequest(jsonify("false"))
    }
  }

  //ASYCNC
  //  def getUserPortfolio = Action.async { request =>
  //    Future {
  //      val userId = getUser(request)
  //      Logger.info("return portfolio for " + userId)
  //      Ok(new UserPortfolio(userId).asJSON)
  //    }
  //  }

  //SYCNC
  //  def getUserPortfolio = Action { request =>
  //    val userId = getUser(request)
  //    Logger.info("return portfolio for " + userId)
  //    Ok(new UserPortfolio(userId).asJSON)
  //  }

  def getUserTradeHistory() = Action.async { request =>
    Future {
      val userId = getUser(request)
      Ok(new UserDBModel().getUserTradeHistoryAsJson(userId))
    }
  }

  //val userStateActor: ActorRef = Akka.system.actorOf(Props.create(classOf[UserStateActor]), "userStateActor")

  def newUser = Action.async(parse.json) { request =>
    Future {
      Logger.info(request.body.toString())
      val userDetail = UserDetail.buildUserDetailFromJSON(request.body.toString())

      val duration = Duration(5, SECONDS)
      implicit val timeout: Timeout = new Timeout(duration)
      //let the user state actor update the database and  wait for its result as a future
      val future = SystemSupervisor.supervisor ask new NewUserRq(userDetail)
      val result = Await.result(future, timeout.duration)
      result match {
        case Success() => Ok(JsObject(Seq("authid" -> JsString(userDetail.getEmail + "535510N" + new NCrypt().hash(userDetail.getEmail))))).withHeaders(
          HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
        )
        case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
        case _ => BadRequest(jsonify("false"))
      }
    }
  }

  def updateSettings = Action.async(parse.json) { request =>
    Future {
      val userId = Global.getUser(request)
      val email = new UserDBModel().view.selectUserByEmail(userId).getEmail
      val userDetail = UserDetail.buildUserDetailFromJSON(request.body.toString())
      userDetail.setEmail(email)

      val duration = Duration(5, SECONDS)
      implicit val timeout: Timeout = new Timeout(duration)
      //let the user state actor update the database and  wait for its result as a future
      val future = SystemSupervisor.supervisor ask new UpdateUserRq(userDetail)
      val result = Await.result(future, timeout.duration)
      result match {
        case Success() => Ok(jsonify("true")).withHeaders(
          HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
        )
        case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
        case _ => BadRequest(jsonify("false"))
      }
    }
  }


  def newTrade = Action.async(parse.json) {
    request =>
      Future {
        Logger.info(request.body.toString())
        val user = getUser(request)
        val trade: Trade = Trade.buildFromJSON(user, request.body.toString())
        val duration = Duration(5, SECONDS)
        implicit val timeout: Timeout = new Timeout(duration)

        //let the user state actor update the database and  wait for its result as a future
        val future = SystemSupervisor.supervisor ask new TradeStock(trade)
        val result = Await.result(future, timeout.duration)
        result match {
          case Success() => Ok(jsonify(request.body.toString())).withHeaders(
            HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
          )
          case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
          case s: String => {
            if (s.equals("ok")) {
              Ok(jsonify("true"))
            }
            else {
              BadRequest(jsonify(s))
            }
          }
          case ex: java.lang.Error => BadRequest(jsonify(ex.getLocalizedMessage))
          case _ => BadRequest(jsonify("false"))
        }
      }
  }

  def levelUp = Action.async(parse.json) {
    request =>
      Future {
        Logger.info(request.body.toString())
        val user = getUser(request)

        val duration = Duration(5, SECONDS)
        implicit val timeout: Timeout = new Timeout(duration)
        //let the user state actor update the database and  wait for its result as a future
        val future = SystemSupervisor.supervisor ask new LevelUpRq(user)
        val result = Await.result(future, timeout.duration)
        result match {
          case Success() => Ok(jsonify("true")).withHeaders(
            HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
          )
          case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
          case _ => BadRequest(jsonify("false"))
        }
      }
  }

  def resetPassword = Action.async(parse.json) { request =>
    Future {
      Logger.info(request.body.toString())
      val user = getUser(request)
      val userDetail = UserDetail.buildUserDetailFromJSON(request.body.toString())
      if (userDetail.getPassword != null && userDetail.getPassword.length > 0) {

        val duration = Duration(5, SECONDS)
        implicit val timeout: Timeout = new Timeout(duration)
        //let the user state actor update the database and  wait for its result as a future
        val future = SystemSupervisor.supervisor ask new NewPasswordRq(user, userDetail.getPassword)
        val result = Await.result(future, timeout.duration)
        result match {
          case Success() => Ok(jsonify("true")).withHeaders(
            HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
          )
          case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
          case _ => BadRequest(jsonify("false"))
        }
      } else {
        BadRequest(jsonify("false"))
      }
    }
  }

  def jsonify(status: String): JsValue = {
    JsObject(Seq(
      "name" -> JsString("status"),
      "value" -> JsString(status)
    ))
  }

  // a sample action using an authorization implementation
  //  def onlyTwitter = SecuredAction(WithProvider("twitter")) { implicit request =>
  //    Ok("You can see this because you logged in using Twitter")
  //  }
  //
  //  def linkResult = SecuredAction { implicit request =>
  //    Ok(views.html.linkResult(request.user))
  //  }

  /**
   * Sample use of SecureSocial.currentUser. Access the /current-user to test it
   */
  //  def currentUser = Action.async { implicit request =>
  //    import play.api.libs.concurrent.Execution.Implicits._
  //    SecureSocial.currentUser[DemoUser].map { maybeUser =>
  //      val userId = maybeUser.map(_.main.userId).getOrElse("unknown")
  //      Ok(s"Your id is $userId")
  //    }
  //  }

}

// An Authorization implementation that only authorizes uses that logged in using twitter
//case class WithProvider(provider: String) extends Authorization[DemoUser] {
//  def isAuthorized(user: DemoUser, request: RequestHeader) = {
//    user.main.providerId == provider
//  }
//}
