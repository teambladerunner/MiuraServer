package controllers

import akka.util.Timeout
import model.SystemSupervisor
import model.stocks.{GetQuote, StocksDBModel}
import play.api.libs.json._
import play.api.mvc.{Action, _}
import play.mvc.Http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.pattern.ask

object StockSearch extends Controller {

  def search(symbol: String) = Action.async { request =>
    Future {
      //Global.getUser(request)
      val duration = Duration(2, SECONDS)
      implicit val timeout: Timeout = new Timeout(duration)

      val stockSentiment: Future[JsValue] = StockSentiment.getForServer(symbol)
      //val current = new java.math.BigDecimal(Global.stockQuote.newPrice(symbol, 0.0).toString)
      val marketPriceFuture = SystemSupervisor.supervisor ask new GetQuote(symbol)
      val current = new java.math.BigDecimal(Await.result(marketPriceFuture, timeout.duration).asInstanceOf[Double].toString)
      val nasdaqStockInfo = Option(new StocksDBModel().view.getStockDailyInfo(symbol))
      val open = nasdaqStockInfo match {
        case None => new java.math.BigDecimal("0.0")
        case _ => new java.math.BigDecimal(nasdaqStockInfo.get.getLastSaleRate.toString)
      }
      val marketCap = nasdaqStockInfo match {
        case None => new java.math.BigDecimal("0.0")
        case _ => new java.math.BigDecimal(nasdaqStockInfo.get.getMarketCap.toString)
      }

      val stockSummary: JsValue = //fetch daily summary
        JsObject(Seq(
          "current_price" -> JsNumber(current),
          "open_price" -> JsNumber(open),
          "market_cap" -> JsNumber(marketCap),
          "day_change_amount" -> JsNumber(current.subtract(open)),
          "day_change_percent" -> JsNumber((current.doubleValue() / open.doubleValue()) * 100)
        ))

      val json: JsValue = JsObject(Seq(
        "symbol" -> JsString(symbol),
        "stock_summary" -> stockSummary,
        try {
          "stock_sentiment" -> Await.result(stockSentiment, timeout.duration)
        } catch {
          case exception: Exception => "stock_sentiment" -> StockSentiment.defaultSentimentJson()
        }
      ))

      Ok(json).withHeaders(
        HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
      )
      //      case Error(_) => BadRequest(jsonify(result.asInstanceOf[Error].description))
      //      case _ => BadRequest(jsonify("false"))
    }
  }

}
