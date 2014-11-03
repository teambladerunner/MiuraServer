package model.stocks

import java.math

import akka.actor.UntypedActor
//import model.stocks.{StockHistory, StockUpdate, StocksActor, WatchStock}
//import play.api.Logger
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json._
import collection.JavaConversions._

import scala.math.BigDecimal

/**
 * The broker between the WebSocket and the StockActor(s).  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */
class StockFeedPublishActorSSE(symbol: String, sseChannel: (Enumerator[JsValue], Concurrent.Channel[JsValue])) extends UntypedActor {

  StocksActor.stocksActor.tell(new WatchStock(symbol), getSelf)

  @scala.throws[Exception](classOf[Exception])
  override def onReceive(message: Any): Unit = {
    if (message.isInstanceOf[StockUpdate]) {
      val stockUpdate: StockUpdate = message.asInstanceOf[StockUpdate]
      val json: JsValue = JsObject(Seq(
        "type" -> JsString("stockupdate"),
        "symbol" -> JsString(stockUpdate.symbol),
        "price" -> JsNumber(new BigDecimal(new java.math.BigDecimal(stockUpdate.price.toString)))
      ))
      sseChannel._2.push(json)
    }
    else if (message.isInstanceOf[StockHistory]) {
      val stockHistory: StockHistory = message.asInstanceOf[StockHistory]
      var history: JsArray = JsArray()
      for (price: Number <- stockHistory.history) {
        history = history.append(
          JsObject(Seq
            ("price" -> JsNumber(new BigDecimal(new java.math.BigDecimal(price.toString))))
          )
        )
      }

      val json: JsValue = JsObject(Seq(
        "type" -> JsString("stockhistory"),
        "symbol" -> JsString(stockHistory.symbol),
        "history" -> history
      ))
      sseChannel._2.push(json)
    }
  }

}