package model.stocks

import akka.actor.Actor
import play.api.Logger

import scala.io.Source

class SymbolUploadActor extends Actor {

  val stocksDBModel = new StocksDBModel()

  override def receive: Receive = {
    case Upload => {
      uploadNasdaqQuotes
      //send a message to stock actor
    }
    case _ =>
  }

  def uploadNasdaqQuotes(): Unit = {
    // get the csv file for NASDAQ symbols and latest prices
    try {
      val csvFile = Source.fromURL("http://www.nasdaq.com/screening/companies-by-name.aspx?letter=0&exchange=nasdaq&render=download")
      // a csv file iterator with the first line dropped (headings), split into an array on "," symbol
      val linesIterator = csvFile.getLines().drop(1).map(_.split("\",\""))
      for (line <- linesIterator) {
        //val (symbol, lastSale, marketCap, sector, industry) = (line(0), line(2), line(3), line(6), line(7))
        def nasdaqData = (line(0), line(2), line(3), line(6), line(7))
        try {
          stocksDBModel.insertNASDAQDataToTable(nasdaqData)
        } catch {
          case exception: Exception => Console.println(exception.toString + " " + nasdaqData._2 + " , " + nasdaqData._3)
        }
      }
      stocksDBModel.updateUploadTime()
    } catch {
      case exception: Exception => Logger.info("exception fetching days nasdaq feed -> " + exception.getLocalizedMessage)
    }
  }

}

case object Upload
