package ru.helper.logger

import akka.event.LoggingAdapter
import ru.core.actors.ActorProcessingSupervisor.MainTask
import ru.database.quillmodels.Woman

object  LoggerMain {
  def httpResponse(mainTask: MainTask, woman: Woman, httpString: String)(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} ACTOR_RESPONSE=$httpString ")
  def httpRequest(mainTask: MainTask, woman: Woman, httpString: String)(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} ACTOR_REQEST=$httpString ")

  def newListLogger(mainTask: MainTask, woman: Woman, list: List[Any])(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} NEW_LIST=${list.toString()}")

  def oldListLogger(mainTask: MainTask, woman: Woman, list: List[Any])(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} OLD_LIST=${list.toString()}")

  def startActorLogger(nameActor: String, mainTask: MainTask, option: Option[Woman] = None)(
      implicit log: LoggingAdapter): Unit = option match {
    case Some(woman) =>
      log.info(s"START=$nameActor TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman}")
    case None => log.info(s"START=$nameActor TASK_ID=${mainTask.task.idtask}")
  }

  def stopActorLogger(nameActor: String, mainTask: MainTask, option: Option[Woman] = None)(implicit log: LoggingAdapter): Unit =
    option match {
      case Some(woman) => log.info(s"STOP=$nameActor TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman}")
      case None        => log.info(s"STOP=$nameActor TASK_ID=${mainTask.task.idtask}")
    }
  //----------------------------------------------------------------------------------------------------------------------------------
  def womanAddFriend(mainTask: MainTask, woman: Woman, man: Int)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.info(
      s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} ADD_FRIEND MAN=$man BLACKLIST=${mainTask.task.blacklist} requestID=$requestID")

  def womanAddGroup(mainTask: MainTask, woman: Woman, man: Int)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.info(
      s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} ADD_GROUP MAN=$man GROUP_ID=${mainTask.task.blacklist} requestID=$requestID")

  def errorNumber(mainTask: MainTask, woman: Woman, num_error: Int)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} ERROR=$num_error requestID=$requestID")

  def error15AccessDenied(mainTask: MainTask, woman: Woman, man: Int)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.info(
      s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} ACCESS_DENIED MAN=$man BLACKLIST=${mainTask.task.blacklist} requestID=$requestID")

  def request(mainTask: MainTask, woman: Woman, request: String)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} requestID=$requestID request=$request")

  def response(mainTask: MainTask, woman: Woman, response: String)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} requestID=$requestID response=$response")

  def manHaveFriend(mainTask: MainTask, woman: Woman, man: Int, manHaveFriend: Int)(implicit log: LoggingAdapter,
                                                                                    requestID: String): Unit =
    log.info(
      s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} MAN=$man HAVE_FRIEND=$manHaveFriend requestID=$requestID")

  def manHaveFriendNeed(mainTask: MainTask, woman: Woman, man: Int, manHaveFriend: Int)(implicit log: LoggingAdapter,
                                                                                        requestID: String): Unit =
    log.info(
      s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} MAN=$man HAVE_FRIEND=$manHaveFriend NEED=${mainTask.searchlist.get.min_add_friend_man} requestID=$requestID")

  def errorCaptcha(mainTask: MainTask, woman: Woman)(implicit log: LoggingAdapter, requestID: String): Unit =
    log.error(s"TASK_ID=${mainTask.task.idtask} WOMAN_ID=${woman.idwoman} requestID=$requestID ERROR_CAPTCHA")
  //----------------------------------------------------------------------------------------------------------------------------------

  def nextWoman(mainTask: MainTask, woman: Woman)(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} NEXT_WOMAN_ID=$woman")

  def oldWoman(mainTask: MainTask, woman: Int)(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} OLD_WOMAN_ID=$woman")

  def nextWomanError(mainTask: MainTask, woman: Woman)(implicit log: LoggingAdapter): Unit =
    log.info(s"TASK_ID=${mainTask.task.idtask} NEXT_WOMAN_ID_ERROR=$woman")

}
