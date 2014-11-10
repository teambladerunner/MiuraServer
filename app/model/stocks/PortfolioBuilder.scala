package model.stocks

import akka.pattern.ask
import akka.util.Timeout
import model.SystemSupervisor
import model.dataobjects.{JSONify, Trade, UserStock}

import scala.collection._
import scala.concurrent.Await
import scala.concurrent.duration._

class PortfolioBuilder {

  def buildPortfolio(userStocks: Seq[UserStock], trades: Seq[Trade]): UserPortfolioSummary = {

    //val tradesParList = trades.par.map(trade => (trade.getSymbol, trade))
    val tradesParList = trades.map(trade => (trade.getSymbol, trade))
    val tradesMap = tradesParList.groupBy(_._1).mapValues(_.map(_._2))
    var portfolio: List[UserStockSummary] = List()

    var totalCurrentMarketPrice: Double = 0.0
    var totalValue = 0.0

    //userStocks.par.foreach(userStock => {
    userStocks.foreach(userStock => {
      val duration = Duration(5, SECONDS)
      implicit val timeout: Timeout = new Timeout(duration)

      val symbol = userStock.getSymbol
      //val currentMarketPrice = new GoogleAPIStockQuote().newPrice(symbol, 0.0).toFloat
      val marketPriceFuture = SystemSupervisor.supervisor ask new GetQuote(symbol)
      val currentMarketPrice = Await.result(marketPriceFuture, timeout.duration).asInstanceOf[Double].toFloat
      //val tradeList: Option[parallel.ParSeq[Trade]] = tradesMap.get(symbol)
      totalCurrentMarketPrice = totalCurrentMarketPrice + currentMarketPrice
      val tradeList: Option[Seq[Trade]] = tradesMap.get(symbol)

      var totalBuys = 0
      var totalSells = 0
      var totalUnits: Float = 0.0F
      var totalBuyPrice: Float = 0.0F
      var totalSellPrice: Float = 0.0F

      tradeList.get.foreach(trade => {
        if (trade.getBuySell.equals("B")) {
          // if buy add to buy count
          totalBuys = totalBuys + 1
          // if buy add to quantity else subtract = total quantity
          totalUnits = totalUnits + trade.getUnits
          // if buy add to total buy price
          totalBuyPrice = totalBuyPrice + trade.getRate
          totalValue = totalValue + (totalUnits * currentMarketPrice)
        } else {
          // if sell add to sell count
          totalSells = totalSells + 1
          // if buy add to quantity else subtract = total quantity
          totalUnits = totalUnits + trade.getUnits //sells are stored as negative
          totalSellPrice = totalSellPrice + trade.getRate
        }
      })

      // total units * current market price = current value
      // divide total buy price / buy count for average buy price
      val userStockSummary = new UserStockSummary(symbol, totalUnits, (totalBuyPrice / totalBuys), totalBuyPrice,
        currentMarketPrice, (currentMarketPrice * totalUnits),
        (((currentMarketPrice * totalUnits) % (totalBuyPrice * totalUnits)) * 100),
        ((currentMarketPrice * totalUnits) - (totalBuyPrice * totalUnits)),
        0.0F, 0.0F)
      portfolio = portfolio :+ userStockSummary
    })
    return new UserPortfolioSummary(totalValue, portfolio)
  }

}

case class UserPortfolioSummary(currentValue: Double, portfolio: List[UserStockSummary])

case class UserStockSummary(symbol: String, totalUnits: Float, averageInvestedPrice: Float, totalPurchasePrice: Float,
                            currentMarketPrice: Float, currMarketValue: Float, realizedProfitPercentage: Float, realizedProfitAmount: Float,
                            unrealizedProfitPercentage: Float, unrealizedProfitAmount: Float) extends JSONify
