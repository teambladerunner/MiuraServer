package model

import akka.actor.SupervisorStrategy._
import akka.actor._
import akka.pattern.ask
import akka.routing.ConsistentHashingRouter
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import akka.util.Timeout
import model.dataobjects.UserDetail
import model.stocks._
import model.user._

import scala.concurrent.Await
import scala.concurrent.duration._

class Supervisor extends Actor {

  def userStateHashMapping: ConsistentHashMapping = {
    case NewUserRq(userDetail: UserDetail) => userDetail.getEmail
    case UpdateUserRq(userDetail: UserDetail) => userDetail.getEmail
    //case NewTradeRq(trade: Trade) => trade.getUser
    case LevelUpRq(user: String) => user
    case NewPasswordRq(user: String, password: String) => user
    case s: String => s
  }

  val routed = ActorSystem().actorOf(Props[UserStateActor].withRouter(
    ConsistentHashingRouter(5, hashMapping = userStateHashMapping)),
    name = "userStateCache")

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }

  var lastSender = ActorSystem().deadLetters

  val duration = Duration(5, SECONDS)
  implicit val timeout: Timeout = new Timeout(duration)

  def receive = {
    //STOCK STATE REQUESTS
    case quoteAndAvail@QuoteAndAvail(symbol, quote, available) => {
      context.child(symbol + "EXA").getOrElse {
        val child = context.actorOf(Props(new ExecuteTradeActor(symbol)), symbol + "EXA")
        context.watch(child)
        child
      } forward quoteAndAvail
    }
    case watchTrades@StartWatchingTrades(symbol) => {
      context.child(symbol + "EXA").getOrElse {
        val child = context.actorOf(Props(new ExecuteTradeActor(symbol)), symbol + "EXA")
        context.watch(child)
        child
      } forward watchTrades
    }
    case fetchLatest@GetQuote(symbol) => {
      context.child(symbol).getOrElse {
        val child = context.actorOf(Props(new StockActor(symbol)), symbol)
        context.watch(child)
        child
      } forward fetchLatest
    }
    case updateSymbol@UpdateSymbol(symbol, tradeType, tradedUnits) => {
      context.child(symbol).getOrElse {
        val child = context.actorOf(Props(new StockActor(symbol)), symbol)
        context.watch(child)
        child
      } forward updateSymbol
    }
    case getQuoteAndAvail@GetQuoteAndAvail(symbol) => {
      context.child(symbol).getOrElse {
        val child = context.actorOf(Props(new StockActor(symbol)), symbol)
        context.watch(child)
        child
      } forward getQuoteAndAvail
    }
    case tradeStock@TradeStock(trade) =>
      // get or create the StockActor for the symbol and forward this message
      context.child(trade.getSymbol).getOrElse {
        val child = context.actorOf(Props(new StockActor(trade.getSymbol)), trade.getSymbol)
        context.watch(child)
        child
      } forward tradeStock
    case watchStock@WatchStock(symbol) =>
      // get or create the StockActor for the symbol and forward this message
      context.child(symbol).getOrElse {
        val child = context.actorOf(Props(new StockActor(symbol)), symbol)
        context.watch(child)
        child
      } forward watchStock
    case unwatchStock@UnwatchStock(Some(symbol)) =>
      // if there is a StockActor for the symbol forward this message
      context.child(symbol).foreach(_.forward(unwatchStock))
    case unwatchStock@UnwatchStock(None) =>
      // if no symbol is specified, forward to everyone
      context.children.foreach(_.forward(unwatchStock))

    // USER STATE REQUESTS
    case request@NewUserRq(userDetail) => routed forward request
    case request@UpdateUserRq(userDetail) => routed forward request
    //case request@NewTradeRq(trade) => routed forward request //TODO deprecate this one
    case request@LevelUpRq(user) => routed forward request
    case request@NewPasswordRq(user, password) => routed forward request
    case Terminated(_) => lastSender ! "finished"
    case _ => println("unknown")
  }

}

object SystemSupervisor {
  lazy val supervisor: ActorRef = ActorSystem().actorOf(Props.create(classOf[Supervisor]), "supervisor")
}