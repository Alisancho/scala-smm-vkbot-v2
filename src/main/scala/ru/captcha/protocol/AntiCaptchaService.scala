package ru.captcha.protocol

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import play.api.libs.json.{Json, OFormat}
import ru.AppStart.config
import ru.http.thisclass.ClassProxyRest

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object AntiCaptchaService {
  private lazy val KEY_CAPTCHA      = config.getString("KEY_CAPTCHA")
  private lazy val URL_CAPTCH_IN    = config.getString("URL_CAPTCH_IN")
  private lazy val URL_CAPTCH_RES   = config.getString("URL_CAPTCH_RES")
  private lazy val CAPTCHA_phrase   = config.getString("CAPTCHA_phrase")
  private lazy val CAPTCHA_regsense = config.getString("CAPTCHA_regsense")
  private lazy val CAPTCHA_numeric  = config.getString("CAPTCHA_numeric")
  private lazy val CAPTCHA_min_len  = config.getString("CAPTCHA_min_len")
  private lazy val CAPTCHA_max_len  = config.getString("CAPTCHA_max_len")
  private lazy val CAPTCHA_language = config.getString("CAPTCHA_language")
  private lazy val CAPTCHA_lang     = config.getString("CAPTCHA_lang")

  private lazy val NUMBER_OF_ATTEMPTS        = config.getString("NUMBER_OF_ATTEMPTS").toInt
  private lazy val INTERVAL_BETWEEN_REQUESTS = config.getString("INTERVAL_BETWEEN_REQUESTS").toInt
  private lazy val ruCaptchaURLPOST =
    Uri(URL_CAPTCH_IN).withQuery(
      Query("key" → KEY_CAPTCHA, "json" → "1")
    )

  def apply(implicit classProxyRest: ClassProxyRest,
            ec: ExecutionContextExecutor,
            materializer: ActorMaterializer,
            log: LoggingAdapter) =
    new AntiCaptchaService()

  implicit lazy val captchaResp: OFormat[CaptchaResp] =
    Json.format[CaptchaResp]

  case class CaptchaResp(status: Int, request: String)

}

class AntiCaptchaService(implicit classProxyRest: ClassProxyRest,
                         ec: ExecutionContextExecutor,
                         materializer: ActorMaterializer,
                         log: LoggingAdapter) {

  import AntiCaptchaService._

  def getCapcha(byteString: ByteString): Future[Option[String]] =
    (for {
      idCaptcha <- classProxyRest.multypatrt(ruCaptchaURLPOST.toString(), byteString)
      jsonRes   <- Future { Json.parse(idCaptcha).as[CaptchaResp] }
      z         <- new_data_gen(ruCaptchaURLGET(jsonRes.request).toString(), classProxyRest)
    } yield z).recover {
      case e => { log.error(s"ERROR_CAPTCHA ${e.getLocalizedMessage}"); None }
    }

  private def ruCaptchaURLGET(ID: String): Uri =
    Uri(URL_CAPTCH_RES)
      .withQuery(
        Query(
          "key"    → KEY_CAPTCHA,
          "json"   → "1",
          "action" → "get",
          "id"     → ID,
        )
      )

  private def new_data_gen(idCaptcha: String, classProxyRestL: ClassProxyRest) =
    Source(Seq.range(0, NUMBER_OF_ATTEMPTS)).flatMapConcat { а =>
      Source.single(idCaptcha).delay(INTERVAL_BETWEEN_REQUESTS.seconds)
    }.mapAsync(1)(k => classProxyRestL.get(k.toString))
      .filter(l => { Json.parse(l.toString).as[CaptchaResp].status == 1 })
      .take(1)
      .runWith(Sink.headOption)
}
