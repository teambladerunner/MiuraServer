package model.stocks

import java.lang

import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class GoogleAPIStockQuote extends StockQuote {

  override def newPrice(symbol: String, lastPrice: lang.Double): lang.Double = {
    getFromGoogle1(symbol, lastPrice)
  }

  import play.api.Play.current

  def getFromGoogle1(symbol: String, lastPrice: Double): Double = {
    val googleUrl = "http://www.google.com/finance/info?q=NASDAQ:" + symbol
    val futureResult: Future[String] = WS.url(googleUrl).get().map {
      response => response.body.toString
    }
    try {
      val result2 = Await.result(futureResult, 2 second)
      val jsonStr = result2.substring(4, result2.toString.length - 1)
      val json: JsValue = Json.parse(jsonStr)
      val currentPrices: Seq[String] = (json \\ "l_cur").map(_.as[String])
      play.api.Logger.info("fetched current google api price for " + symbol + " " + currentPrices(0).toDouble)
      return currentPrices(0).toDouble
    } catch {
      case exception: Exception => {
        play.api.Logger.info(exception.getLocalizedMessage)
        return lastPrice
      }
    }
  }

}
