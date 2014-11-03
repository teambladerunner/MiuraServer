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
    val result2 = Await.result(futureResult, 2 second)
    try {
      val jsonStr = result2.substring(4, result2.toString.length - 1)
      play.api.Logger.info(jsonStr)
      val json: JsValue = Json.parse(jsonStr)
      val currentPrices: Seq[String] = (json \\ "l_cur").map(_.as[String])
      return currentPrices(0).toDouble
    } catch {
      case exception: Exception => {
        play.api.Logger.info("error substring for " + result2)
        return lastPrice
      }
    }
  }

  //  implicit val formats = DefaultFormats
  //
  //  def getFromGoogle(symbol:String, lastPrice: Double): Double = {
  //    val googleUrl = "http://www.google.com/finance/info?q=NASDAQ:" + symbol
  //    val page = url(googleUrl)
  //    val response = Http(page OK dispatch.as.json4s.Json)
  //    response onComplete {
  //      case Success(json) => {
  //        val currentPrice =  (json \ "l").extract[Double]
  //        return currentPrice
  //      }
  //      case Failure(error) => {
  //        Logger.info(error.getLocalizedMessage)
  //        return lastPrice
  //      }
  //    }
  //    return lastPrice
  //  }

}
