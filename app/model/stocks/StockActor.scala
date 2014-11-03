package model.stocks

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.libs.Akka

import scala.collection.JavaConverters._
import scala.collection.immutable.{HashSet, Queue}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * There is one StockActor per stock symbol.  The StockActor maintains a list of users watching the stock and the stock
 * values.  Each StockActor updates a rolling dataset of randomly generated stock values.
 */

class StockActor(symbol: String) extends Actor {

  lazy val stockQuote: StockQuote = new GoogleAPIStockQuote

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]

  var stockHistory: Queue[java.lang.Double] = Queue()

  // Fetch the latest stock value every 2 minutes
  val stockTick = context.system.scheduler.schedule(Duration.Zero, 2.minutes, self, FetchLatest)

  def receive = {
    case FetchLatest =>
      // add a new stock price to the history and drop the oldest
      if (stockHistory.size == 0) {
        val newPrice = stockQuote.newPrice(symbol, 0.0) //TODO may get opening price
        Logger.info("got new price " + symbol + " -> " + newPrice)
        stockHistory = stockHistory :+ newPrice
        // notify watchers
        watchers.foreach(_ ! StockUpdate(symbol, newPrice))
      } else {
        val newPrice = stockQuote.newPrice(symbol, stockHistory.last.doubleValue())
        Logger.info("got new price " + symbol + " -> " + newPrice)
        stockHistory = stockHistory.drop(1) :+ newPrice
        // notify watchers
        watchers.foreach(_ ! StockUpdate(symbol, newPrice))
      }
    case WatchStock(_) =>
      // send the stock history to the user
      sender ! StockHistory(symbol, stockHistory.asJava)
      // add the watcher to the list
      watchers = watchers + sender
    case UnwatchStock(_) =>
      watchers = watchers - sender
      if (watchers.size == 0) {
        stockTick.cancel()
        context.stop(self)
      }
  }
}

class StocksActor extends Actor {
  def receive = {
    case watchStock@WatchStock(symbol) =>
      // get or create the StockActor for the symbol and forward this message
      context.child(symbol).getOrElse {
        context.actorOf(Props(new StockActor(symbol)), symbol)
      } forward watchStock
    case unwatchStock@UnwatchStock(Some(symbol)) =>
      // if there is a StockActor for the symbol forward this message
      context.child(symbol).foreach(_.forward(unwatchStock))
    case unwatchStock@UnwatchStock(None) =>
      // if no symbol is specified, forward to everyone
      context.children.foreach(_.forward(unwatchStock))
  }
}

object StocksActor {
  lazy val stocksActor: ActorRef = Akka.system.actorOf(Props(classOf[StocksActor]))
}


case object FetchLatest

case class StockUpdate(symbol: String, price: Number)

case class StockHistory(symbol: String, history: java.util.List[java.lang.Double])

case class WatchStock(symbol: String)

case class UnwatchStock(symbol: Option[String])