package controllers

import akka.util.Timeout
import model.stocks.StocksDBModel
import play.api.libs.json._
import play.api.mvc.{Action, _}
import play.mvc.Http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object StockSearch extends Controller {

  def search(symbol: String) = Action.async { request =>
    Future {
      val duration = Duration(10, SECONDS)
      implicit val timeout: Timeout = new Timeout(duration)

      val stockSentiment: Future[JsValue] = StockSentiment.getForServer(symbol)
      val current = new java.math.BigDecimal(Global.stockQuote.newPrice(symbol, 0.0).toString)
      val nasdaqStockInfo = new StocksDBModel().springJDBCQueries.getStockDailyInfo(symbol)
      val open = new java.math.BigDecimal(nasdaqStockInfo.getLastSaleRate.toString)

      val stockSummary: JsValue = //fetch daily summary
        JsObject(Seq(
          "current_price" -> JsNumber(current),
          "open_price" -> JsNumber(open),
          "market_cap" -> JsNumber(new java.math.BigDecimal(nasdaqStockInfo.getMarketCap.toString)),
          "day_change_amount" -> JsNumber(current.subtract(open)),
          "day_change_percent" -> JsNumber((current.doubleValue() / open.doubleValue()) * 100)
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
