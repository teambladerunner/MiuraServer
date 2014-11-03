package controllers.filters

import play.api.Logger
import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CORSFilter extends Filter {

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    Logger.info("filtering /OPTIONS request from " + rh.remoteAddress)

    val result = f(rh)
    result.map(_.withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
      HeaderNames.ACCESS_CONTROL_ALLOW_METHODS -> "POST, GET, OPTIONS, PUT, DELETE",
      HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS -> "x-requested-with,content-type,Cache-Control,Pragma,Date, authid"
    ))
  }

}

