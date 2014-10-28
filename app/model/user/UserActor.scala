package model.user

import akka.actor.UntypedActor
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import model.dataobjects.UserStock
import model.stocks.{StockHistory, StockUpdate, StocksActor, WatchStock}
import play.libs.Json
import play.mvc.WebSocket

/**
 * The broker between the WebSocket and the StockActor(s).  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */
class UserActor(out: WebSocket.Out[JsonNode]) extends UntypedActor {

  val defaultStocks: Seq[UserStock] = new UserDBModel().getUserStocks("zubin")

  for (userStock <- defaultStocks) {
    StocksActor.stocksActor.tell(new WatchStock(userStock.getSymbol), getSelf)
  }

  @scala.throws[Exception](classOf[Exception])
  override def onReceive(message: Any): Unit = {
    if (message.isInstanceOf[StockUpdate]) {
      val stockUpdate: StockUpdate = message.asInstanceOf[StockUpdate]
      val stockUpdateMessage: ObjectNode = Json.newObject
      stockUpdateMessage.put("type", "stockupdate")
      stockUpdateMessage.put("symbol", stockUpdate.symbol)
      stockUpdateMessage.put("price", stockUpdate.price.doubleValue)
      out.write(stockUpdateMessage)
    }
    else if (message.isInstanceOf[StockHistory]) {
      val stockHistory: StockHistory = message.asInstanceOf[StockHistory]
      val stockUpdateMessage: ObjectNode = Json.newObject
      stockUpdateMessage.put("type", "stockhistory")
      stockUpdateMessage.put("symbol", stockHistory.symbol)
      val historyJson: ArrayNode = stockUpdateMessage.putArray("history")
      import scala.collection.JavaConversions._
      for (price <- stockHistory.history) {
        historyJson.add((price.asInstanceOf[Number]).doubleValue)
      }
      out.write(stockUpdateMessage)
    }
  }
}