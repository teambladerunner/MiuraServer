package model.user

import akka.actor.UntypedActor
import model.dataobjects.UserDetail
import play.api.Logger

class UserStateActor extends UntypedActor {

  @scala.throws[Exception](classOf[Exception])
  override def onReceive(message: Any): Unit = {
    if (message.isInstanceOf[NewUserRq]) {
      Logger.info("got user state message " + message)
      val newUserRq: NewUserRq = message.asInstanceOf[NewUserRq]
      new UserDBModel().createUser(newUserRq.userDetail)
    }
  }
}

case class NewUserRq(userDetail: UserDetail)
