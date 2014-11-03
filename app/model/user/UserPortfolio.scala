package model.user

import model.dataobjects.json.JSONifier
import model.dataobjects.{Trade, UserStock}
import model.stocks.{PortfolioBuilder, UserStockSummary}

class UserPortfolio(user: String) {

  val userStocks: Seq[UserStock] = new UserDBModel().getUserStocks(user)

  val userTradeHistory: Seq[Trade] = new UserDBModel().getUserTradeHistory(user)

  val portfolio: List[UserStockSummary] = new PortfolioBuilder().buildPortfolio(userStocks, userTradeHistory)

  def asJSON = JSONifier.toJSON(portfolio)

}
