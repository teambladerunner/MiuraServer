package controllers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable, Props}
import com.github.nscala_time.time.Imports._
import controllers.filters.{AccessLog, CORSFilter}
import model.stocks.{SymbolUploadActor, Upload}
import org.springframework.context.support.ClassPathXmlApplicationContext
import play.api.mvc.WithFilters
import play.api.{Logger, _}

import scala.compat.Platform
import scala.concurrent.duration.Duration

//object Global extends WithFilters(SecurityHeadersFilter()) with GlobalSettings {
object Global extends WithFilters(AccessLog, CORSFilter) with GlobalSettings {
  //object Global extends GlobalSettings {

  val context = new ClassPathXmlApplicationContext("application-context.xml")

  val millisInADay = 86400000L

  var scheduledItems: List[Cancellable] = List()

  override def onStart(app: play.api.Application): Unit = {
    super.onStart(app)
    val system = ActorSystem()
    import system.dispatcher

    Logger.info("getting stock download actor...")
    val nasdaqSymbolUploadActor = system.actorOf(Props(classOf[SymbolUploadActor]), "nasdaqSymbolUploadActor") //TODO can i abstract this?

    //    val lastUpdateUploadTime = new StocksDBModel().getLastUpdateUploadTime()
    //    Logger.info("last updated " + lastUpdateUploadTime)
    //    // if last update was yesterday and next update tomorrow upload now

    // in all cases schedule the next upload for next 8:30pm EST
    var nasdaqOpen = DateTime.now.withZone(DateTimeZone.forID("EST")).withHourOfDay(20).withMinuteOfHour(30)
    if (DateTime.now.getMillis > nasdaqOpen.getMillis) {
      nasdaqOpen = nasdaqOpen.plusDays(1)
    }

    Logger.info("scheduling next upload at " + nasdaqOpen.withZone(DateTimeZone.forID("UTC")) + " UTC")
    val scheduled =
      system.scheduler.schedule(Duration.create(nasdaqOpen.getMillis - Platform.currentTime, TimeUnit.MILLISECONDS),
        Duration.create(1, TimeUnit.DAYS),
        nasdaqSymbolUploadActor,
        Upload)
    scheduledItems = scheduledItems :+ scheduled

  }

  override def onStop(app: play.api.Application): Unit = {
    super.onStop(app)
    Logger.info("Application shutdown...")
    for (scheduled <- scheduledItems) {
      scheduled.cancel
    }
  }


}
