package ru.http.thisclass

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.GenericHttpCredentials
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.{ClientTransport, Http}
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import ru.database.quillmodels.Proxyserver

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
object ClassProxyRest {
  def apply(
      proxy: Proxyserver)(implicit system: ActorSystem, ec: ExecutionContext, materializer: Materializer): ClassProxyRest =
    new ClassProxyRest(proxy)
}

class ClassProxyRest(val proxy: Proxyserver)(implicit
                                             system: ActorSystem,
                                             ec: ExecutionContext,
                                             materializer: Materializer)
    extends LazyLogging {

  private val proxyAddress =
    InetSocketAddress.createUnresolved(proxy.ip, proxy.port)
  val auth                        = headers.BasicHttpCredentials(proxy.login, proxy.pass)
  private val httpsProxyTransport = ClientTransport.httpsProxy(proxyAddress, auth)
  private val settings = ConnectionPoolSettings(system)
    .withConnectionSettings(
      ClientConnectionSettings(system)
        .withTransport(httpsProxyTransport)
    )
  val wsClient = StandaloneAhcWSClient()

  def get(url: String): Future[String] =
    Http()
      .singleRequest(HttpRequest(uri = url), settings = settings)
      .flatMap(x => Unmarshal(x).to[String])

  def getBytes(url: String): Future[ByteString] =
    Http()
      .singleRequest(HttpRequest(uri = url), settings = settings)
      .flatMap(x => Unmarshal(x).to[ByteString])

  def multypatrt(myurl: String, res: ByteString): Future[String] =
    for {
      entity      <- Marshal(getMultipart(res)).to[MessageEntity]
      entityBytes <- entity.toStrict(100.seconds)
      header      = GenericHttpCredentials(scheme = "Signature", token = "")
      request = HttpRequest(HttpMethods.POST, myurl)
        .addCredentials(header)
        .withEntity(HttpEntity(entity.contentType, entityBytes.getData()))

      result <- Http()
                 .singleRequest(request, settings = settings)
                 .flatMap(_.entity.toStrict(100.second))
                 .map(_.getData().utf8String)
    } yield result

  private def getMultipart(res: ByteString) = {
    Multipart.FormData(
      Multipart.FormData.BodyPart(
        "file",
        HttpEntity
          .Strict(MediaTypes.`application/octet-stream`, res),
        Map("filename" -> "cat.jpg")
      ),
    )
  }
}
