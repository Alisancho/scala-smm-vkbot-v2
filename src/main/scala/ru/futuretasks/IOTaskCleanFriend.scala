package ru.futuretasks

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.{ActorMaterializer, SharedKillSwitch}
import cats.effect.IO
import play.api.libs.json.Json
import ru.MyContext
import ru.captcha.protocol.AntiCaptchaService
import ru.database.quillmodels.Woman
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPIFriend
import ru.vk.response.JSONVKError

import scala.concurrent.ExecutionContextExecutor

object IOTaskCleanFriend {
  def apply(classProxyRest: ClassProxyRest, woman: Woman)(
      implicit system: ActorSystem,
      ec: ExecutionContextExecutor,
      ctx: MyContext,
      materializer: ActorMaterializer
  ) = new IOTaskCleanFriend(classProxyRest, woman)
}

class IOTaskCleanFriend(classProxyRest: ClassProxyRest, woman: Woman)(
    implicit val system: ActorSystem,
    ec: ExecutionContextExecutor,
    ctx: MyContext,
    materializer: ActorMaterializer
) extends VKAPIFriend with FutureTaskHelper {
  import ru.vk.response.VKObject._
  private val ruCap = AntiCaptchaService(classProxyRest, ec, materializer, log)
  def getIOTask(ID_MAN: Int, sharedKillSwitch: SharedKillSwitch) = IO {
    rest(deleteFriend(ID_MAN, woman.access_token).toString())

    def rest(request: String): Unit = {
      log.info(s"IO_CLEAN WOMAN_ID=${woman.idwoman} request=$request")
      classProxyRest
        .get(request)
        .foreach(u => {
          log.info(s"IO_CLEAN WOMAN_ID=${woman.idwoman} response=$u")
          if (u.contains("error_code")) {
            val myError = Json.parse(u).as[JSONVKError]

            myError.error.error_code match {
              case 1 | 3 | 5 | 6 | 10 | 15 | 17 | 18 | 100 | 117 | 174 | 177 => {
                sharedKillSwitch.shutdown()
              }
              case 14 =>
                (for {
                  m       <- IO.fromFuture(IO(classProxyRest.getBytes(myError.error.captcha_img.get)))
                  strJson <- IO.fromFuture(IO(ruCap.getCapcha(m))).handleErrorWith(_ => IO { None })
                  newio   <- captchaHelper(strJson)(deleteFriend(ID_MAN, woman.access_token, _, myError.error.captcha_sid.get))
                  _ <- IO {
                        newio match {
                          case q: Uri => rest(q.toString())
                          case _      => {}
                        }
                      }
                } yield ()).unsafeRunSync()
              case _ => {
                sharedKillSwitch.shutdown()
              }
            }
          } else {}
        })
    }
  }
}
