package ru.futuretasks

import java.time.Instant
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import cats.effect.IO
import play.api.libs.json.Json
import ru.MyContext
import ru.captcha.protocol.AntiCaptchaService
import ru.core.actors.ActorProcessingSupervisor.MainTask
import ru.core.actors.ActorTask.NextWoman
import ru.database.query.{QueryCondition, QueryFriendList, QueryTaskWoman, QueryWoman}
import ru.database.quillmodels.{ConditionSave, FriendList, TaskWoman, Woman}
import ru.helper.logger.LoggerMain
import ru.http.thisclass.ClassProxyRest
import ru.vk.response.JSONVKError
import ru.vk.{VKAPI, VKAPIFriend}

import scala.concurrent.ExecutionContextExecutor

object IOTaskFriendList {
  def apply(
      woman: Woman,
      mainTask: MainTask
  )(actorTask: ActorRef)(
      implicit system: ActorSystem,
      ec: ExecutionContextExecutor,
      ctx: MyContext,
      materializer: ActorMaterializer,
      classProxyRest: ClassProxyRest
  ): IOTaskFriendList =
    new IOTaskFriendList(woman, mainTask)(
      actorTask = actorTask
    )
}
class IOTaskFriendList(
    woman: Woman,
    val mainTask: MainTask
)(actorTask: ActorRef)(
    implicit val system: ActorSystem,
    val ec: ExecutionContextExecutor,
    val ctx: MyContext,
    val classProxyRest: ClassProxyRest,
    materializer: ActorMaterializer
) extends FutureTaskHelper with VKAPIFriend with VKAPI with QueryCondition with QueryWoman with QueryTaskWoman
    with QueryFriendList {

  import ru.vk.response.VKObject._
  private val ruCap = AntiCaptchaService(classProxyRest, ec, materializer, log)
  def getIOTask(friendList: FriendList): IO[Unit] = IO {
    implicit val requestID: String = UUID.randomUUID().toString
    val request =
      addFriend(ID_MAN = friendList.id_man, access_token = woman.access_token).toString
    rest(request.toString)

    def rest(request: String): Unit = {
      LoggerMain.request(mainTask, woman, request)
      classProxyRest
        .get(request)
        .foreach(response ⇒ {
          LoggerMain.response(mainTask, woman, response)
          if (response.contains("error_code")) {
            val myError = Json.parse(response).as[JSONVKError]

            myError.error.error_code match {
              case 1 ⇒ {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 3 ⇒ {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 5 ⇒ {
                updateWomanStatus(woman.copy(status = "ERROR_5"))
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 6 ⇒ {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 10 => {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 14 ⇒
                (for {
                  _       <- IO { LoggerMain.errorNumber(mainTask, woman, myError.error.error_code) }
                  m       <- IO.fromFuture(IO(classProxyRest.getBytes(myError.error.captcha_img.get)))
                  strJson <- IO.fromFuture(IO(ruCap.getCapcha(m))).handleErrorWith(_ => IO { None })
                  newio <- captchaHelper(strJson)(
                            addFriend(ID_MAN = friendList.id_man,
                                      access_token = woman.access_token,
                                      _: String,
                                      captcha_sid = myError.error.captcha_sid.get))
                  _ <- IO {
                        newio match {
                          case q: Uri => rest(q.toString())
                          case _      => { LoggerMain.errorCaptcha(mainTask, woman) }
                        }
                      }
                } yield ()).unsafeRunSync()

              case 15 ⇒ {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
                updateFriendListStatus(
                  friendList.copy(status = "ACCESS_DENIED", id_woman = woman.idwoman, data_time_add = Instant.now()))
              }
              case 17 ⇒ {
                updateWomanStatus(woman.copy(status = "NEED_VALIDATION"))
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 18 => {
                updateWomanStatus(woman.copy(status = "BUN"))
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 100 ⇒ {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
                updateFriendListStatus(
                  friendList.copy(status = "ERROR_100", id_woman = woman.idwoman, data_time_add = Instant.now()))
              }
              case 117 ⇒ {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 103 ⇒ {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 177 => {
                updateFriendListStatus(
                  friendList.copy(status = "DONE", id_woman = woman.idwoman, data_time_add = Instant.now()))
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case _ ⇒ {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, 0)
              }

            }
            //actor ! NextID()

          } else {
            LoggerMain.womanAddFriend(mainTask, woman, friendList.id_man)
            updateFriendListStatus(friendList.copy(status = "DONE", id_woman = woman.idwoman, data_time_add = Instant.now()))

            updateTaskWomanAddOne(
              TaskWoman(
                idtask = mainTask.task.idtask,
                status = "ACTIVE",
                idwoman = woman.idwoman,
                current_day = 1,
                datatime_start = Instant.now(),
                datatime_latest_addition = Instant.now(),
                datatime_stop = Instant.now()
              )
            )

            updateConditionAddOne(
              ConditionSave(
                idtask = mainTask.task.idtask,
                current_day = 1,
                datatime_start_task = Instant.now(),
                datatime_latest_addition = Instant.now()
              ))
          }
        })
    }
  }
}
