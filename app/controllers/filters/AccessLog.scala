package controllers.filters

import play.api.mvc._

import scala.concurrent.Future

object AccessLog extends Filter {
  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val result = f(rh)
    play.Logger.info(rh + "\n\t => " + result)
    result
  }
}
