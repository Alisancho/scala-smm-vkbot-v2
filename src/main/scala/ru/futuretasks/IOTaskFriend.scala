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
import ru.core.actors.ActorTask.{NextWoman, PlusOne}
import ru.database.query.{QueryBlackListFriend, QueryCondition, QueryTaskWoman, QueryWoman}
import ru.database.quillmodels.{BlackListFriend, ConditionSave, TaskWoman, Woman}
import ru.helper.logger.LoggerMain
import ru.http.thisclass.ClassProxyRest
import ru.vk.response.{JSONVKError, JSONVKFriendsGet}
import ru.vk.{VKAPI, VKAPIFriend}

import scala.concurrent.ExecutionContextExecutor

object IOTaskFriend {
  def apply(classProxyRest: ClassProxyRest, blacklist: String, woman: Woman, mainTask: MainTask)(actorTask: ActorRef)(
      implicit system: ActorSystem,
      ec: ExecutionContextExecutor,
      ctx: MyContext,
      materializer: ActorMaterializer
  ) = new IOTaskFriend(classProxyRest, blacklist, woman, mainTask)(actorTask)
}

class IOTaskFriend(classProxyRest: ClassProxyRest, blacklist: String, woman: Woman, mainTask: MainTask)(actorTask: ActorRef)(
    implicit val system: ActorSystem,
    val ec: ExecutionContextExecutor,
    val ctx: MyContext,
    materializer: ActorMaterializer
) extends FutureTaskHelper with VKAPIFriend with VKAPI with QueryCondition with QueryWoman with QueryTaskWoman
    with QueryBlackListFriend {
  import ru.vk.response.VKObject._
  private val ruCap = AntiCaptchaService(classProxyRest, ec, materializer, log)
  def getIOTask(ID_MAN: Int) = IO {
    implicit val requestID: String = UUID.randomUUID().toString
    classProxyRest
      .get(
        getFriends(access_token = woman.access_token, ID_USER = ID_MAN).toString
      )
      .foreach(k ⇒ {
        if (k.contains("error_code")) {
          val myError = Json.parse(k).as[JSONVKError]
          if (myError.error.error_code == 15) {

            LoggerMain.error15AccessDenied(mainTask, woman, ID_MAN)

            setBlackListFriend(
              BlackListFriend(
                namelist = blacklist,
                idman = ID_MAN,
                idwoman = woman.idwoman,
                datatime = Instant.now(),
                reason = "ACCESS_DENIED",
              ))
          }
        } else {
          val manHaveFriend = Json.parse(k).as[JSONVKFriendsGet]
          if (manHaveFriend.response.count >= mainTask.searchlist.get.min_add_friend_man) {
            LoggerMain.manHaveFriend(mainTask, woman, ID_MAN, manHaveFriend.response.count)
            val request =
              addFriend(ID_MAN = ID_MAN, access_token = woman.access_token).toString
            rest(request.toString)
          } else {
            LoggerMain.manHaveFriendNeed(mainTask, woman, ID_MAN, manHaveFriend.response.count)
            setBlackListFriend(
              BlackListFriend(
                namelist = blacklist,
                idman = ID_MAN,
                idwoman = woman.idwoman,
                datatime = Instant.now(),
                reason = "FRIEND_HAVE=" + manHaveFriend.response.count,
              ))
          }
        }
      })

    def rest(request: String): Unit = {
      LoggerMain.request(mainTask, woman, request)
      classProxyRest
        .get(request)
        .foreach(response ⇒ {
          LoggerMain.response(mainTask, woman, response)
          if (response.contains("error_code")) {
            val myError = Json.parse(response).as[JSONVKError]

            myError.error.error_code match {
              case 1 | 3 | 103 | 117 | 174 => {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 5 => {
                updateWomanStatus(woman.copy(status = "ERROR_5"))
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 6 | 10 => {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 14 =>
                (for {
                  _       <- IO { LoggerMain.errorNumber(mainTask, woman, myError.error.error_code) }
                  m       <- IO.fromFuture(IO(classProxyRest.getBytes(myError.error.captcha_img.get)))
                  strJson <- IO.fromFuture(IO(ruCap.getCapcha(m))).handleErrorWith(_ => IO { None })
                  newio <- captchaHelper(strJson)(
                            addFriend(ID_MAN = ID_MAN,
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

              case 15 => {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
                setBlackListFriend(
                  BlackListFriend(
                    namelist = blacklist,
                    idman = ID_MAN,
                    idwoman = woman.idwoman,
                    datatime = Instant.now(),
                    reason = "ACCESS_DENIED"
                  ))
              }
              case 17 => {
                updateWomanStatus(woman.copy(status = "NEED_VALIDATION"))
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 18 => {
                updateWomanStatus(woman.copy(status = "BUN"))
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case 100 => {
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
                setBlackListFriend(
                  BlackListFriend(
                    namelist = blacklist,
                    idman = ID_MAN,
                    idwoman = woman.idwoman,
                    datatime = Instant.now(),
                    reason = "TEST"
                  ))
              }
              case 177 => {
                setBlackListFriend(
                  BlackListFriend(
                    namelist = blacklist,
                    idman = ID_MAN,
                    idwoman = woman.idwoman,
                    datatime = Instant.now(),
                    reason = "OK",
                  ))
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }
              case _ => {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, 0)
              }

            }
            //actor ! NextID()

          } else {
            setBlackListFriend(
              BlackListFriend(
                namelist = blacklist,
                idman = ID_MAN,
                idwoman = woman.idwoman,
                datatime = Instant.now(),
                reason = "OK",
              ))

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

            actorTask ! PlusOne(woman.idwoman)
            LoggerMain.womanAddFriend(mainTask, woman, ID_MAN)
          }
        })
    }
  }
}
