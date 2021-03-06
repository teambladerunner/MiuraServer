package controllers

import java.lang.reflect.Constructor
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable, Props}
import com.github.nscala_time.time.Imports._
import controllers.filters.{AccessLog, CORSFilter, LoggingFilter}
import model.SystemSupervisor
import model.stocks._
import model.user.hash.DCrypt
import model.user.{DemoUser, InMemoryUserService, MyEventListener}
import org.springframework.context.support.ClassPathXmlApplicationContext
import play.api.mvc.{Cookie, Request, WithFilters}
import play.api.{Logger, _}
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.GoogleProvider

import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap
import scala.compat.Platform
import scala.concurrent.duration.Duration

//object Global extends WithFilters(SecurityHeadersFilter()) with GlobalSettings {
object Global extends WithFilters(AccessLog, LoggingFilter, CORSFilter) with GlobalSettings {
  //object Global extends GlobalSettings {

  import scala.concurrent.ExecutionContext.Implicits.global

  val system = ActorSystem()

  val nasdaqSymbolUploadActor = system.actorOf(Props(classOf[SymbolUploadActor]), "nasdaqSymbolUploadActor")

  val context = new ClassPathXmlApplicationContext("application-context.xml")

  val millisInADay = 86400000L

  var scheduledItems: List[Cancellable] = List()

  override def onStart(app: play.api.Application): Unit = {
    System.setProperty("java.net.preferIPv4Stack", "true")
    Logger.info("IP v4 settings = " + System.getProperty("java.net.preferIPv4Stack") + " and mode = " + play.api.Play.current.mode)
    super.onStart(app)
    //
    //    Logger.info("getting stock download actor...")
    //    val nasdaqSymbolUploadActor = system.actorOf(Props(classOf[SymbolUploadActor]), "nasdaqSymbolUploadActor") //TODO can i abstract this?

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

    val pendingSymbols = new StocksDBModel().view.getPendingSymbols()
    for (pendingSymbol <- pendingSymbols) {
      SystemSupervisor.supervisor ! (new StartWatchingTrades(pendingSymbol))
    }
  }

  override def onStop(app: play.api.Application): Unit = {
    super.onStop(app)
    Logger.info("Application shutdown...")
    for (scheduled <- scheduledItems) {
      scheduled.cancel
    }
  }

  /**
   * The runtime environment for this sample app.
   */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[DemoUser] {
    override lazy val routes = new CustomRoutesService()
    override lazy val userService: InMemoryUserService = new InMemoryUserService()
    override lazy val providers = ListMap(
      //      include(
      //        new FacebookProvider(routes, cacheService, oauth2ClientFor(FacebookProvider.Facebook))
      //      ),
      include(
        new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))
      ) //,
      //      include(
      //        new LinkedInOAuth2Provider(routes, cacheService, oauth2ClientFor(LinkedInOAuth2Provider.LinkedIn))
      //      ),
      //      include(
      //        new LinkedInProvider(routes, cacheService, oauth1ClientFor(LinkedInProvider.LinkedIn))
      //      ),
      //      include(
      //        new UsernamePasswordProvider[DemoUser](userService, avatarService, viewTemplates, passwordHashers)
      //      )
    )
    override lazy val eventListeners = List(new MyEventListener())
  }

  def getUser(request: Request[AnyRef]): String = {
    val cookie: Option[Cookie] = request.cookies.get("authid")
    val headerValue: Option[String] = request.headers.get("authid")
    headerValue match {
      case None => throw new Exception("You are not logged in")
      case _ => validateUser(headerValue)
    }
  }

  def validateUser(someOption: Option[String]): String = {
    someOption match {
      case None => ""
      case _ => {
        val components = someOption.get.split("535510N")
        if (components.size > 1) {
          val email = components(0)
          val hash = components(1)
          if (new DCrypt().validatePassword(email, hash)) {
            return email
          }
        }
      }
    }
    throw new Exception("invalid user id")
  }

  /**
   * An implementation that checks if the controller expects a RuntimeEnvironment and
   * passes the instance to it if required.
   *
   * This can be replaced by any DI framework to inject it differently.
   *
   * @param controllerClass
   * @tparam A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[DemoUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

}
