package model.user

import model.dataobjects.{Trade, UserStock}
import model.stocks.{UserPortfolioSummary, PortfolioBuilder, UserStockSummary}
import play.api.libs.json._

class UserPortfolio(user: String) {

  val userStocks: Seq[UserStock] = new UserDBModel().getUserStocks(user)

  val userTradeHistory: Seq[Trade] = new UserDBModel().getUserTradeHistory(user)

  val portfolio: UserPortfolioSummary = {
    play.api.Logger.info("built portfolio for " + user)
    new PortfolioBuilder().buildPortfolio(userStocks, userTradeHistory)
  }

  implicit val UserStockSummary = new Writes[UserStockSummary] {
    def writes(userStockSummary: UserStockSummary) = Json.obj(
      "averageInvestedPrice" -> userStockSummary.averageInvestedPrice,
      "currentMarketPrice" -> userStockSummary.currentMarketPrice,
      "currMarketValue" -> userStockSummary.currMarketValue,
      "realizedProfitAmount" -> userStockSummary.realizedProfitAmount,
      "realizedProfitPercentage" -> userStockSummary.realizedProfitPercentage,
      "symbol" -> userStockSummary.symbol,
      "totalPurchasePrice" -> userStockSummary.totalPurchasePrice,
      "totalUnits" -> userStockSummary.totalUnits,
      "unrealizedProfitAmount" -> userStockSummary.unrealizedProfitAmount,
      "unrealizedProfitPercentage" -> userStockSummary.unrealizedProfitPercentage)
  }

  val json: JsValue = JsObject(Seq(
    "user" -> JsString(user),
    "cash_balance" -> JsNumber(new UserDBModel().springJDBCQueries.selectUserByEmail(user).getCash),
    "current_vale_total" -> JsNumber(portfolio.currentValue),
    "stocks" -> Json.toJson(portfolio.portfolio)
  ))

  def asJSON = Json.stringify(json)

}
