package controllers

import akka.actor.{ActorRef, Props}
import controllers.Global._
import model.SystemSupervisor
import model.dataobjects.json.JSONifier
import model.stocks.{StockFeedPublishActorSSE, StocksDBModel, WatchStock}
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller}
import play.libs.Akka

import scala.concurrent.Future

object StockFeed extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def sseSymbolFeed(symbol: String) = Action {
    implicit req => {
      val (out, wsOutChannel) = Concurrent.broadcast[JsValue]
      val userActor: ActorRef = Akka.system.actorOf(Props(new StockFeedPublishActorSSE(symbol, (out, wsOutChannel))))
      val watchStock: WatchStock = new WatchStock(symbol)
      //StocksActor.stocksActor.tell(watchStock, userActor)
      SystemSupervisor.supervisor.tell(watchStock, userActor)
      //Ok.feed(out &> EventSource()).as("text/event-stream")
      play.api.Logger.info("setting feed channel for " + symbol)
      Ok.chunked(out &> EventSource()).as("text/event-stream")
    }
  }

  def getAllStocks = Action.async { request =>
    Future {
      val userId = getUser(request)
      Ok(JSONifier.toJSON(new StocksDBModel().view.getAllStocks))
    }
  }

}
