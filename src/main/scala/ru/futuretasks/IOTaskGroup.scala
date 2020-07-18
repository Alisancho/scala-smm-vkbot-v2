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
import ru.database.query.{QueryBlackListGroup, QueryCondition, QueryTaskWoman, QueryWoman}
import ru.database.quillmodels.{BlackListGroup, ConditionSave, TaskWoman, Woman}
import ru.helper.logger.LoggerMain
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPIGroup
import ru.vk.response.JSONVKError

import scala.concurrent.ExecutionContextExecutor

object IOTaskGroup {
  def apply(
      blacklist: String,
      woman: Woman,
      mainTask: MainTask,
  )(actorTask: ActorRef)(
      implicit system: ActorSystem,
      ec: ExecutionContextExecutor,
      ctx: MyContext,
      classProxyRest: ClassProxyRest,
      materializer: ActorMaterializer
  ): IOTaskGroup =
    new IOTaskGroup(blacklist, woman, mainTask)(actorTask: ActorRef)
}
class IOTaskGroup(blacklist: String, woman: Woman, mainTask: MainTask)(
    actorTask: ActorRef
)(implicit val system: ActorSystem,
  val ec: ExecutionContextExecutor,
  val ctx: MyContext,
  classProxyRest: ClassProxyRest,
  materializer: ActorMaterializer)
    extends FutureTaskHelper with VKAPIGroup with QueryWoman with QueryBlackListGroup with QueryCondition with QueryTaskWoman {
  import ru.vk.response.VKObject._
  private val ruCap = AntiCaptchaService(classProxyRest, ec, materializer, log)
  def getIOTask(ID_MAN: Int): IO[Unit] = IO {
    implicit val requestID: String = UUID.randomUUID().toString
    val request =
      inviteToGroup(
        ID_MAN = ID_MAN,
        access_token = woman.access_token,
        groupID = blacklist
      ).toString
    rest(request.toString)

    def rest(request: String): Unit = {
      LoggerMain.request(mainTask, woman, request)
      classProxyRest
        .get(request)
        .foreach(response ⇒ {
          if (response.contains("error_code")) {
            LoggerMain.response(mainTask, woman, response)
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
                            inviteToGroup(ID_MAN = ID_MAN,
                                          access_token = woman.access_token,
                                          groupID = blacklist,
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
                setBlackListGroup(
                  BlackListGroup(
                    idgroup = blacklist,
                    idman = ID_MAN,
                    idwoman = woman.idwoman,
                    datatime = Instant.now(),
                    reason = "ACCESS_DENIED"
                  ))
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
                setBlackListGroup(
                  BlackListGroup(
                    idgroup = blacklist,
                    idman = ID_MAN,
                    idwoman = woman.idwoman,
                    datatime = Instant.now(),
                    reason = "TEST"
                  ))
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
                setBlackListGroup(
                  BlackListGroup(
                    idgroup = blacklist,
                    idman = ID_MAN,
                    idwoman = woman.idwoman,
                    datatime = Instant.now(),
                    reason = "OK",
                  ))
                LoggerMain.errorNumber(mainTask, woman, myError.error.error_code)
              }

              case _ ⇒ {
                actorTask ! NextWoman(woman.idwoman)
                LoggerMain.errorNumber(mainTask, woman, 0)
              }

            }
            //actor ! NextID()

          } else {
            LoggerMain.response(mainTask, woman, response)

            setBlackListGroup(
              BlackListGroup(
                idgroup = blacklist,
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
            LoggerMain.womanAddGroup(mainTask, woman, ID_MAN)
          }
        })
    }
  }
}
