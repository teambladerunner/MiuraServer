package model.user

import anorm._
import model.dataobjects.db.{DB, DBFacade}
import model.dataobjects.{Trade, UserDetail, UserStock}
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConversions._
import scala.compat.Platform

class UserDBModel extends DBFacade {

  def getUserStocks(userId: String): Seq[UserStock] = springJDBCQueries.getUserStocks(userId)

  def getUserTradeHistory(userId: String): Seq[Trade] = springJDBCQueries.getUserTradeHistory(userId)

  def getUserStocksAsJson(userId: String): JsObject = {
    var jsObjects: List[JsObject] = List()
    for (userStock: UserStock <- new UserDBModel().getUserStocks(userId)) {
      jsObjects = jsObjects :+ Json.obj(
        "stockSymbol" -> userStock.getSymbol,
        "units" -> userStock.getUnits.toString
      )
    }
    return Json.obj(
      "user" -> userId,
      "userStocks" -> jsObjects
    )
  }

  def getUserTradeHistoryAsJson(userId: String): JsObject = {
    var jsObjects: List[JsObject] = List()
    for (trade <- new UserDBModel().getUserTradeHistory(userId)) {
      jsObjects = jsObjects :+ Json.obj(
        "dateTime" -> trade.getTime.toString,
        "stockSymbol" -> trade.getSymbol,
        "units" -> trade.getUnits.toString,
        "rate" -> trade.getRate.toString,
        "buySell" -> trade.getBuySell
      )
    }
    return Json.obj(
      "user" -> userId,
      "userTrades" -> jsObjects
    )
  }

  def createUser(userDetail: UserDetail): Unit = {
    DB.withConnection { implicit c =>
      SQL("insert into MIURA.GAMEUSER(USERIDPK, PASSWORD, FIRSTNAME ,LASTNAME, EMAIL, TWITTERHANDLE, FACEBOOKHANDLE, GOOGLEHANDLE, LINKEDINHANDLE) " +
        "values ({userIdPk}, {password}, {firstName}, {lastName}, {email}, {twitterHandle}, {facebookHandle}, {googleHandle}, {linkedinHandle})").on(
          "userIdPk" -> Platform.currentTime.toString,
          "password" -> userDetail.getPassword,
          "firstName" -> userDetail.getFirstName,
          "lastName" -> userDetail.getLastName,
          "email" -> userDetail.getEmail,
          "twitterHandle" -> "",
          "facebookHandle" -> "",
          "googleHandle" -> "",
          "linkedinHandle" -> ""
        ).executeUpdate()
    }
  }

}
