package model.stocks

import java.math

import akka.actor.{Actor, ActorRef}
import model.SystemSupervisor
import model.dataobjects.{Trade, UserDetail}
import model.user.UserDBModel
import play.api.Logger

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

  // Fetch the latest stock value every 2 minutes
  val stockTick = context.system.scheduler.schedule(Duration.Zero, 20.seconds, self, FetchLatest)

  val startAvailableUnits = 1000000.00

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]

  var stockHistory: Queue[java.lang.Double] = Queue()
  stockHistory = stockHistory :+ stockQuote.newPrice(symbol, 0.0)

  var unitsAvailable = new StocksDBModel().view.getAvailableUnits(symbol)
  if (unitsAvailable < 0) unitsAvailable = startAvailableUnits

  def receive = {
    case FetchLatest => {
      // add a new stock price to the history and drop the oldest
      val newPrice = stockQuote.newPrice(symbol, stockHistory.last.doubleValue())
      stockHistory = stockHistory.drop(1) :+ newPrice
      // notify watchers
      watchers.foreach(_ ! StockUpdate(symbol, newPrice))
    }
    case GetQuote(_) => sender() ! stockHistory.head
    case fetchLatest@GetQuoteAndAvail(symbol) => {
      sender() ! new QuoteAndAvail(symbol, stockHistory.head, unitsAvailable)
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
    case tradeStock@TradeStock(trade) => this.trade(tradeStock.trade)
    case updateSymbol@UpdateSymbol(symbol,
    tradeType, tradedUnits) => {
      val dbModel = new StocksDBModel()
      if (tradeType.equals("S")) {
        updateSymbolSell(tradedUnits, dbModel)
      } else {
        updateSymbolBuy(tradedUnits, dbModel)
      }
    }
    case _ => Logger.info("unknown")
  }

  def trade(trade: Trade): Unit = {
    Logger.info("got user trade message " + trade.getUser + " " + trade.getSymbol)
    val userDbModel: UserDBModel = new UserDBModel()
    val userDetailOpt: Option[UserDetail] = Option(userDbModel.view.selectUserByEmail(trade.getUser))
    userDetailOpt match {
      case None => sender() ! new Error("The email address does not exist")
      case _ => {
        val dbModel = new StocksDBModel();
        if (trade.getBuySell.equalsIgnoreCase("B") && unitsAvailable < trade.getUnits) {
          sender() ! new Error("There are not enough units on the market to trade")
          return
        } else {
          val userDetail = userDetailOpt.get
          val margin = ((userDetail.getLevel * stockHistory.head) / 100)
          if (((trade.getRate - stockHistory.head) >= margin && ((stockHistory.head - trade.getRate) >= margin))) {
            sender() ! new Error("Your user level is not high enough to trade this spread. You can trade within {Your Level} % Market Price ")
          } else {
            if (trade.getBuySell.equalsIgnoreCase("S")) {
              val userStocksAvailable = new StocksDBModel().view.getUserStocksAvailable(trade.getUser, trade.getSymbol)
              if (userStocksAvailable != null && userStocksAvailable.get(0).getPrice < trade.getUnits) {
                sender() ! new Error("There are not enough units in your portfolio to trade")
                return
              } // else {
              //                updateSymbolSell(trade.getUnits.doubleValue(), trade.getSymbol, dbModel)
              //              }
            } else {
              if (userDetail.getCash.doubleValue().toFloat < trade.getUnits * stockHistory.head) {
                sender() ! new Error("There is not enough cash in your portfolio to trade")
                return
              } //else {
              //                updateSymbolBuy(trade.getUnits.doubleValue(), trade.getSymbol, dbModel)
              //              }
            }
            Logger.info("going ahead with " + trade.getUser + " " + trade.getSymbol)
            updateUser(userDetail, trade, userDbModel)
            SystemSupervisor.supervisor.tell(new QuoteAndAvail(symbol, stockHistory.head, unitsAvailable), self)
          }
        }
      }
    }
    Logger.info("done trade " + trade.getUser + " " + trade.getSymbol)
    sender() ! "ok"
  }


  def updateUser(userDetail: UserDetail, trade: Trade, userDbModel: UserDBModel) = {
    //update cash
    val cashTraded = trade.getRate * trade.getUnits
    if (trade.getBuySell.equalsIgnoreCase("B")) {
      userDetail.setCash(userDetail.getCash.subtract(new math.BigDecimal(cashTraded.toString)))
      userDbModel.createTrade(trade, trade.getUnits)
    } else {
      userDetail.setCash(userDetail.getCash.add(new math.BigDecimal(cashTraded.toString)))
      userDbModel.createTrade(trade, trade.getUnits * -1) //negate for sell
    }
    //update level
    val level = ((userDbModel.view.getUserTradeCount(trade.getUser) + 1) / 5) + 1
    userDetail.setLevel(level)
    userDbModel.updateUser(userDetail)
  }

  def updateSymbolBuy(tradedUnits: Double, dbModel: StocksDBModel) = {
    if (unitsAvailable == startAvailableUnits) {
      unitsAvailable = startAvailableUnits - tradedUnits
      dbModel.createAvailableUnits(symbol)
    } else {
      unitsAvailable = unitsAvailable - tradedUnits
      dbModel.updateAvailableUnits(unitsAvailable - tradedUnits, symbol)
    }
  }

  def updateSymbolSell(tradedUnits: Double, dbModel: StocksDBModel) = {
    unitsAvailable = unitsAvailable + tradedUnits
    dbModel.updateAvailableUnits(unitsAvailable + tradedUnits, symbol)
  }
}

case object FetchLatest

case class GetQuote(symbol: String)

case class GetQuoteAndAvail(symbol: String)

case class StockUpdate(symbol: String, price: Number)

case class StockHistory(symbol: String, history: java.util.List[java.lang.Double])

case class WatchStock(symbol: String)

case class UnwatchStock(symbol: Option[String])

case class TradeStock(trade: Trade)

case class UpdateSymbol(symbol: String, tradeType: String, tradedUnits: Double)