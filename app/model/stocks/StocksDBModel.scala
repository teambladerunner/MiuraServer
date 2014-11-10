package model.stocks

import java.math
import java.sql.Timestamp

import anorm._
import model.dataobjects.db.{DB, DBFacade}

import scala.compat.Platform

class StocksDBModel extends DBFacade {

  def getLastUpdateUploadTime(): Timestamp = {
    val lastUploadValue = view.getEnvProps("LASTNASDAQUPLOAD").getValue
    return new Timestamp(lastUploadValue.toLong)
  }

  def updateUploadTime(): Unit = {
    DB.withTransaction { implicit c =>
      SQL("UPDATE MIURA.MIURAENV SET PROPVALUE = {currentTime} WHERE PROPKEY = {lastNasdaqUpload}").on(
        "currentTime" -> Platform.currentTime,
        "lastNasdaqUpload" -> "LASTNASDAQUPLOAD"
      ).executeUpdate()
    }
  }

  def insertNASDAQDataToTable(nasdaqData: (String, String, String, String, String)): Unit = {
    DB.withTransaction { implicit c =>
      SQL("insert into MIURA.NASDAQUPDATE(TIMEPK, SYMBOL, LASTSALE ,MARKETCAP, SECTOR, INDUSTRY) values ({timePk}, {symbol}, {lastSale}, {marketCap}, {sector}, {industry})").on(
        "timePk" -> Platform.currentTime,
        "symbol" -> nasdaqData._1.replace("\"", ""),
        "lastSale" -> new math.BigDecimal(nasdaqData._2),
        "marketCap" -> new math.BigDecimal(nasdaqData._3),
        "sector" -> nasdaqData._4,
        "industry" -> nasdaqData._5
      ).executeUpdate()
    }
  }

  def updateAvailableUnits(units : Double, symbol: String): Unit = {
    DB.withTransaction { implicit c =>
      SQL("UPDATE MIURA.SYMBOLUNITS SET MARKETUNITS = {units} WHERE SYMBOL = {symbol}").on(
        "units" -> units,
        "symbol" -> symbol
      ).executeUpdate()
    }
  }

  def updatePending(pkId : String): Unit = {
    DB.withTransaction { implicit c =>
      SQL("UPDATE MIURA.USERPORTFOLIO SET PENDING = {pending} WHERE TIMEPK = {pkid}").on(
        "pkid" -> pkId,
        "pending" -> "N"
      ).executeUpdate()
    }
  }

  def createAvailableUnits(symbol: String): Unit = {
    DB.withTransaction { implicit c =>
      SQL("insert into MIURA.SYMBOLUNITS(SYMBOL, MARKETUNITS) values ({symbol}, {marketUnits})").on(
        "symbol" -> symbol,
        "marketUnits" -> new math.BigDecimal("10000.00")
      ).executeUpdate()
    }
  }
}
