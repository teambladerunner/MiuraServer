package controllers

import akka.util.Timeout
import play.api.libs.json._
import play.api.mvc.{Action, _}
import play.mvc.Http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object StockSearch extends Controller {

  def search(symbol: String) = Action.async { request =>
    Future {
      //val userId = Global.getUser(request)
      val duration = Duration(5, SECONDS)
      implicit val timeout: Timeout = new Timeout(duration)

      val stockSentiment: Future[JsValue] = StockSentiment.getForServer(symbol)
      val stockSummary: JsValue = //fetch daily summary
        JsObject(Seq(
          "current_price" -> JsNumber(new java.math.BigDecimal(Global.stockQuote.newPrice(symbol, 0.0).toString)),
          "open_price" -> JsNumber(3.0),
          "market_cap" -> JsNumber(10.0),
          "day_change_amount" -> JsNumber(10.0),
          "day_change_percent" -> JsNumber(10.0)
        ))

      val json: JsValue = JsObject(Seq(
        "symbol" -> JsString(symbol),
        "stock_summary" -> stockSummary,
        "stock_sentiment" ->  Await.result(stockSentiment, timeout.duration)
      ))

      Ok(json).withHeaders(
        HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
      )
      //      case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
      //      case _ => BadRequest(jsonify("false"))
    }
  }

}
