package http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import ru.database.quillmodels.Proxyserver
import ru.http.thisclass.ClassProxyRest

import scala.concurrent.duration._

class ClassProxyRestTest extends FlatSpec with MockFactory with ScalaFutures with Matchers {
  implicit val system             = ActorSystem()
  implicit val ec                 = system.dispatcher
  implicit val materializer       = ActorMaterializer()
  implicit val timeout: Timeout   = Timeout(60 seconds)
  implicit val oo: PatienceConfig = PatienceConfig(60 second) //Нужен для  Future
  implicit val myProxy = Proxyserver(
    idproxyserver = "odkwdkwodk",
    ip = "46.161.50.120",
    port = 20242,
    login = "5b1d107865",
    pass = "6dfb57f531",
    //  comment = "efef",
    status = "OK",
  )

  behavior of "Test Proxy server IP"

  it should "with  ClassProxy" in {

    val calssProxy = ClassProxyRest(myProxy)
    calssProxy
      .get("https://api.ipify.org/")
      .futureValue shouldBe myProxy.ip
  }
}
