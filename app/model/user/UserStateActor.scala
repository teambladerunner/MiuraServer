package model.user

import java.math.BigDecimal
import java.sql.Timestamp

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
            val userDetail = newUserRq.userDetail
            userDetail.setLevel(1)
            userDetail.setCash(new BigDecimal("25000.00"))
            userDetail.setJoinDate(new Timestamp(System.currentTimeMillis()))
            userDbModel.createUser(newUserRq.userDetail) // because the user does not exist
          }
          case _ => {
            sender() ! new Error("The email address is already reserved")
          }
        }
      } else if (message.isInstanceOf[UpdateUserRq]) {
        Logger.info("got user state update message " + message)
        val updateUserRq: UpdateUserRq = message.asInstanceOf[UpdateUserRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val oldUserDetail: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(updateUserRq.userDetail.getEmail))
        oldUserDetail match {
          case None => sender() ! new Error("The email address does not exist")
          case _ => userDbModel.updateUser(UserDetail.mergeForUpdate(updateUserRq.userDetail, oldUserDetail.get))
        }
      } else if (message.isInstanceOf[NewTradeRq]) {
        Logger.info("got user trade message " + message)
        val newTradeRq: NewTradeRq = message.asInstanceOf[NewTradeRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val oldUserDetail: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(newTradeRq.trade.getUser))
        oldUserDetail match {
          case None => sender() ! new Error("The email address does not exist")
          case _ => {
            val level = ((userDbModel.springJDBCQueries.getUserTradeCount(newTradeRq.trade.getUser) + 1) / 5) + 1
            userDbModel.createTrade(newTradeRq.trade)
            val newUserDetail = oldUserDetail.get
            val cashTraded = newTradeRq.trade.getRate * newTradeRq.trade.getUnits
            if (newTradeRq.trade.getBuySell.equalsIgnoreCase("B")) {
              newUserDetail.setCash(newUserDetail.getCash.subtract(new BigDecimal(cashTraded.toString)))
            } else {
              newUserDetail.setCash(newUserDetail.getCash.add(new BigDecimal(cashTraded.toString)))
            }
            newUserDetail.setLevel(level)
            userDbModel.updateUser(newUserDetail)
          }
        }
      } else if (message.isInstanceOf[LevelUpRq]) {
        Logger.info("got level up message " + message)
        val levelUpRq: LevelUpRq = message.asInstanceOf[LevelUpRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val oldUserDetail: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(levelUpRq.user))
        oldUserDetail match {
          case None => sender() ! new Error("The email address does not exist")
          case _ => {
            val newUserDetail = oldUserDetail.get
            val level = newUserDetail.getLevel + 1
            newUserDetail.setLevel(level)
            userDbModel.updateUser(newUserDetail)
          }
        }
      }
      else if (message.isInstanceOf[NewPasswordRq]) {
        Logger.info("got password change message " + message)
        val newPasswordRq: NewPasswordRq = message.asInstanceOf[NewPasswordRq]
        val userDbModel: UserDBModel = new UserDBModel()
        val oldUserDetail: Option[UserDetail] = Option(userDbModel.springJDBCQueries.selectUserByEmail(newPasswordRq.user))
        oldUserDetail match {
          case None => sender() ! new Error("The email address does not exist")
          case _ => {
            val newUserDetail = oldUserDetail.get
            newUserDetail.setPassword(newPasswordRq.password)
            userDbModel.updateUser(newUserDetail)
          }
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

case class UpdateUserRq(userDetail: UserDetail)

case class NewTradeRq(trade: Trade)

case class LevelUpRq(user: String)

case class NewPasswordRq(user: String, password: String)

case class Success()

case class Error(description: String)
