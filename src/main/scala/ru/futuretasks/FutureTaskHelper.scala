package ru.futuretasks

import akka.actor.ActorSystem
import akka.event.LogSource
import akka.http.scaladsl.model.Uri
import cats.effect.IO
import play.api.libs.json.Json
import ru.captcha.protocol.AntiCaptchaService.CaptchaResp
trait FutureTaskHelper {

  import akka.event.Logging

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName

    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }
  val system: ActorSystem

  implicit val log = Logging(system, this)

  def captchaHelper(option: Option[String])(fun: String => Uri) =
    option match {
      case Some(value) =>
        for {
          json <- IO { Json.parse(value).as[CaptchaResp] }
          m    <- IO { fun(json.request) }
        } yield m
      case None => IO.unit
    }

}
