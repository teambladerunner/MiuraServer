package model.stocks

import akka.actor.Actor
import model.SystemSupervisor
import model.dataobjects.Trade

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

class ExecuteTradeActor(symbol: String) extends Actor {

  val executeTick = context.system.scheduler.schedule(Duration.Zero, 10.seconds, self, ExecuteTradesTick)

  def receive = {
    case ExecuteTradesTick => {
      SystemSupervisor.supervisor.tell(new GetQuoteAndAvail(symbol), self)
    }
    case quoteAndAvail: QuoteAndAvail => {
      // get all pending trades sells >= quote and buys <= quote
      val dbModel = new StocksDBModel()
      val pendingSells = dbModel.view.getPendingUserSells(symbol, quoteAndAvail.quote, false)
      var unitsSold = 0.0
      for (pendingSell: Trade <- pendingSells) {
        // update their state to Y
        dbModel.updatePending(pendingSell.getTime.getTime.toString)
        unitsSold = unitsSold + pendingSell.getUnits
      }
      // update symbol
      if (unitsSold > 0.0) {
        SystemSupervisor.supervisor.tell(new UpdateSymbol(symbol, "S", unitsSold), self)
      }

      val pendingBuys = dbModel.view.getPendingUserBuys(symbol, quoteAndAvail.quote, false)
      var unitsBought = 0.0
      var available = quoteAndAvail.availableUnits
      for (pendingBuy: Trade <- pendingBuys) {
        // update their state to Y
        if (pendingBuy.getUnits < available) {
          dbModel.updatePending(pendingBuy.getTime.getTime.toString)
          unitsBought = unitsBought + pendingBuy.getUnits
          available = available - pendingBuy.getUnits
        }
      }
      // update symbol
      if (unitsBought > 0.0) {
        SystemSupervisor.supervisor.tell(new UpdateSymbol(symbol, "B", unitsBought), self)
      }
    }
  }

}

case object ExecuteTradesTick

case class StartWatchingTrades(symbol: String)

case class QuoteAndAvail(symbol: String, quote: Double, availableUnits: Double)