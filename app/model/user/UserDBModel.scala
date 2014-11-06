package model.user

import java.math.BigDecimal

import anorm._
import model.dataobjects.db.{DB, DBFacade}
import model.dataobjects.{Trade, UserDetail, UserStock}
import play.api.Logger
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
        "units" -> "0 units"
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
    DB.withTransaction { implicit c =>
      SQL("insert into MIURA.GAMEUSER(USERIDPK, USERPASSWORD, FIRSTNAME ,LASTNAME, EMAIL, TWITTERHANDLE, FACEBOOKHANDLE, GOOGLEHANDLE, LINKEDINHANDLE, LOCALEID, AVATARID, CASH, JOINDATE) " +
        "values ({userIdPk}, {password}, {firstName}, {lastName}, {email}, {twitterHandle}, {facebookHandle}, {googleHandle}, {linkedinHandle}, {localeId}, {avatarId}, {cash}, {joinDate})").on(
          "userIdPk" -> Platform.currentTime.toString,
          "password" -> userDetail.getPassword,
          "firstName" -> userDetail.getFirstName,
          "lastName" -> userDetail.getLastName,
          "email" -> userDetail.getEmail,
          "twitterHandle" -> "",
          "facebookHandle" -> "",
          "googleHandle" -> "",
          "linkedinHandle" -> "",
          "localeId" -> "",
          "avatarId" -> "",
          "cash" -> userDetail.getCash,
          "level" -> userDetail.getLevel,
      "joinDate" -> userDetail.getJoinDate
        ).executeUpdate()
    }
  }

  def createTrade(trade: Trade): Unit = {
    DB.withTransaction { implicit c =>
      SQL("INSERT INTO MIURA.USERPORTFOLIO(  TIMEPK,  QUOTEID,  SYMBOL,  UNITS,  PRICE,  BUYSELL,  USERID) " +
        "VALUES(  {timepk},  {quoteid},  {symbol},  {units},  {rate},  {buysell},  {userid})").on(
          "timepk" -> Platform.currentTime.toString,
          "quoteid" -> Platform.currentTime.toString,
          "symbol" -> trade.getSymbol,
          "units" -> trade.getUnits,
          "rate" -> trade.getRate,
          "buysell" -> trade.getBuySell,
          "userid" -> trade.getUser
        ).executeUpdate()
    }
  }

  def updateUser(userDetail: UserDetail): Unit = {
    Logger.info(userDetail.getPassword + " " + userDetail.getPkid)
    try {
      DB.withTransaction { implicit c =>
        SQL("update MIURA.GAMEUSER set USERIDPK = {userIdPk}, USERPASSWORD = {password}, FIRSTNAME = {firstName}," +
          "LASTNAME = {lastName}, EMAIL = {email}, TWITTERHANDLE = {twitterHandle}, FACEBOOKHANDLE = {facebookHandle}, " +
          "GOOGLEHANDLE = {googleHandle}, LINKEDINHANDLE = {linkedinHandle}, LOCALEID = {localeId}, AVATARID = {avatarId}, CASH = {cash} " +
          " where USERIDPK = {userIdPk}").on(
            "userIdPk" -> userDetail.getPkid,
            "password" -> userDetail.getPassword,
            "firstName" -> userDetail.getFirstName,
            "lastName" -> userDetail.getLastName,
            "email" -> userDetail.getEmail,
            "twitterHandle" -> "",
            "facebookHandle" -> "",
            "googleHandle" -> "",
            "linkedinHandle" -> "",
            "localeId" -> userDetail.getLocaleId,
            "avatarId" -> userDetail.getAvatarID,
            "cash" -> userDetail.getCash
          ).executeUpdate()
      }
    } catch {
      case exception: Exception => {
        Logger.info(exception.getLocalizedMessage)
        throw exception
      }
    }
  }

}
