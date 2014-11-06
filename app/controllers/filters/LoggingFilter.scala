package controllers.filters

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Iteratee, Enumeratee}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

object LoggingFilter extends EssentialFilter {
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      val startTime = System.currentTimeMillis
      nextFilter(requestHeader).map { result =>
        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime

        val bytesToString: Enumeratee[ Array[Byte], String ] = Enumeratee.map[Array[Byte]]{ bytes => new String(bytes) }
        val consume: Iteratee[String,String] = Iteratee.consume[String]()
        val resultBody : Future[String] = result.body |>>> bytesToString &>> consume

        resultBody.map {
          body =>
            Logger.info(s"${requestHeader.method} ${requestHeader.uri}" +
              s" took ${requestTime}ms and returned ${result.header.status}")
            val jsonBody = Json.parse(body)
            Logger.debug(s"Response\nHeader:\n${result.header.headers.toString}\nBody:\n${Json.prettyPrint(jsonBody)}")
        }

        result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}