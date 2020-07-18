package ru.captcha.protocol

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cats.effect.IO
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import ru.database.quillmodels.Proxyserver
import ru.http.thisclass.ClassProxyRest

import scala.concurrent.Await
import scala.concurrent.duration._
class AntiCaptchaServiceTest extends FlatSpec with MockFactory with ScalaFutures with Matchers {

  implicit val system             = ActorSystem()
  implicit val ec                 = system.dispatcher
  implicit val materializer       = ActorMaterializer()
  implicit val timeout: Timeout   = Timeout(60 seconds)
  implicit val oo: PatienceConfig = PatienceConfig(60 second) //Нужен для  Future
//  implicit val myProxy = Proxyserver(
//    idproxyserver = "odkwdkwodk",
//    ip = "46.161.50.120",
//    port = 20242,
//    login = "5b1d107865",
//    pass = "6dfb57f531",
//    //  comment = "efef",
//    status = "OK",
//  )
//  val capchaURL = "https://api.vk.com/captcha.php?sid=113202231104&s=1"
//  behavior of "AntiCaptchaServiceTest"
//  it should "getCapcha" in {
//    val calssProxy        = ClassProxyRest(myProxy)
//    val antyCapchaService = AntiCaptchaService(calssProxy, ec, materializer,log)
//    val ppp = for {
//      z <- calssProxy.getBytes(capchaURL)
//      _ <- IO { println("___=" + z) }.unsafeToFuture()
//      m <- antyCapchaService.getCapcha(z)
//    } yield m
//    Await.result(ppp, Duration.Inf)
//    println(ppp)

  // }

}
