package controllers

import akka.actor.{ActorRef, _}
import com.fasterxml.jackson.databind.JsonNode
import model.SystemSupervisor
import model.stocks.{UnwatchStock, WatchStock}
import model.user.{DemoUser, UserActor}
import play.libs.{Akka, F}
import play.mvc.WebSocket
import securesocial.core.RuntimeEnvironment

/**
 * The main web controller that handles returning the index page, setting up a WebSocket, and watching a stock.
 */
class Application(override implicit val env: RuntimeEnvironment[DemoUser]) extends securesocial.core.SecureSocial[DemoUser] {

  def index = SecuredAction { implicit request =>
    Ok(views.html.index(request.user.main))
  }

  def ws(): WebSocket[JsonNode] = {
    return new WebSocket[JsonNode] {
      def onReady(in: WebSocket.In[JsonNode], out: WebSocket.Out[JsonNode]) {
        val userActor: ActorRef = Akka.system.actorOf(Props.create(classOf[UserActor], out))
        in.onMessage(new F.Callback[JsonNode] {
          def invoke(jsonNode: JsonNode) {
            val watchStock: WatchStock = new WatchStock(jsonNode.get("symbol").textValue)
            //StocksActor.stocksActor.tell(watchStock, userActor)
            SystemSupervisor.supervisor.tell(watchStock, userActor)
          }
        })
        in.onClose(new F.Callback0 {
          def invoke {
            val none: Option[String] = Option.empty
            //StocksActor.stocksActor.tell(new UnwatchStock(none), userActor)
            SystemSupervisor.supervisor.tell(new UnwatchStock(none), userActor)
            Akka.system.stop(userActor)
          }
        })
      }
    }
  }
}