package model.user

import akka.actor.UntypedActor
import model.dataobjects.{Trade, UserDetail}
import play.api.Logger

class UserStateActor extends UntypedActor {

  @scala.throws[Exception](classOf[Exception])
  override def onReceive(message: Any): Unit = {
    try {
      if (message.isInstanceOf[NewUserRq]) {
        val newUserRq = message.asInstanceOf[NewUserRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val user: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(newUserRq.userDetail.getEmail))
        user match {
          case None => {
            userDbModel.createUser(newUserRq.userDetail) // because the user does not exist
          }
          case _ => {
            sender() ! new Error("The email address is already reserved")
          }
        }
      }
      if (message.isInstanceOf[UpdateUserRq]) {
        Logger.info("got user state update message " + message)
        val updateUserRq: UpdateUserRq = message.asInstanceOf[UpdateUserRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val oldUserDetail: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(updateUserRq.userDetail.getEmail))
        oldUserDetail match {
          case None => sender() ! new Error("The email address does not exist")
          case _ => userDbModel.updateUser(UserDetail.mergeForUpdate(updateUserRq.userDetail, oldUserDetail.get))
        }
      }
      if (message.isInstanceOf[NewTradeRq]) {
        Logger.info("got user trade message " + message)
        val newTradeRq: NewTradeRq = message.asInstanceOf[NewTradeRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val oldUserDetail: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(newTradeRq.trade.getUser))
        oldUserDetail match {
          case None => sender() ! new Error("The email address does not exist")
          case _ => userDbModel.createTrade(newTradeRq.trade)
        }
      }
      sender() ! new Success()
    } catch {
      case exception: Exception => {
        Logger.info(exception.getLocalizedMessage)
        sender() ! new Error(exception.getLocalizedMessage)
      }
    }
  }
}

case class NewUserRq(userDetail: UserDetail)

case class NewTradeRq(trade: Trade)

case class UpdateUserRq(userDetail: UserDetail)

case class Success()

case class Error(description: String)
