package controllers

import akka.actor.{ActorRef, Props}
import controllers.Users._
import model.dataobjects.json.JSONifier
import model.stocks.{StocksDBModel, StockFeedPublishActorSSE, StocksActor, WatchStock}
import model.user.{UserPortfolio, UserActorSSE}
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller}
import play.libs.Akka

import scala.concurrent.Future

object StockFeed extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

//  def sse() = Action {
//    implicit req => {
//      val out = Concurrent.unicast[JsValue](
//        onStart = (c) => {
//          println("started")
//          chans = chans + (nick -> c)
//        },
//        onComplete = {
//          println("disconnected")
//        },
//        onError = (str, in) => {
//          println(str)
//        }
//      ).onDoneEnumerating(
//          println("done")
//        )
//
//      val userActor: ActorRef = Akka.system.actorOf(Props(new UserActorSSE("zubin", (out, wsOutChannel)))) //jsonNode.get("user").textValue
//      val watchStock: WatchStock = new WatchStock("GOOGL") //jsonNode.get("symbol").textValue
//      StocksActor.stocksActor.tell(watchStock, userActor)
//      //Ok.feed(out &> EventSource()).as("text/event-stream")
//      Ok.chunked(out &> EventSource()).as("text/event-stream")
//    }
//  }

  def sse() = Action {
    implicit req => {
      val (out, wsOutChannel) = Concurrent.broadcast[JsValue]
      val userActor: ActorRef = Akka.system.actorOf(Props(new UserActorSSE("zubin", (out, wsOutChannel))))
      val watchStock: WatchStock = new WatchStock("GOOGL")
      StocksActor.stocksActor.tell(watchStock, userActor)
      //Ok.feed(out &> EventSource()).as("text/event-stream")
      Ok.chunked(out &> EventSource()).as("text/event-stream")
    }
  }

  def sseSymbolFeed(symbol: String) = Action {
    implicit req => {
      val (out, wsOutChannel) = Concurrent.broadcast[JsValue]
      val userActor: ActorRef = Akka.system.actorOf(Props(new StockFeedPublishActorSSE(symbol, (out, wsOutChannel))))
      val watchStock: WatchStock = new WatchStock(symbol)
      StocksActor.stocksActor.tell(watchStock, userActor)
      //Ok.feed(out &> EventSource()).as("text/event-stream")
      Ok.chunked(out &> EventSource()).as("text/event-stream")
    }
  }

  def getAllStocks = Action.async { request =>
    Future {
      val userId = getUser(request)
      Ok(JSONifier.toJSON(new StocksDBModel().springJDBCQueries.getAllStocks))
    }
  }

}
