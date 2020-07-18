package ru

import java.time.Instant
import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.{ExitCode, IO, IOApp}
import io.getquill._
import ru.api.rout.MainAPI
import ru.api.task.actorsystem.ActorControllerSupervisor
import ru.api.task.ref._

import scala.language.postfixOps

class MyContext extends MysqlAsyncContext(SnakeCase, "ctx") {
  lazy implicit val instantEncoder = MappedEncoding[Instant, Date] { i =>
    new Date(i.toEpochMilli)
  }

  lazy implicit val instantDecoder = MappedEncoding[Date, Instant] { d =>
    Instant.ofEpochMilli(d.getTime)
  }
}

object AppStart extends IOApp with MainAPI {
  implicit lazy val ctx          = new MyContext
  implicit lazy val system       = ActorSystem("MainActorSystem", config) // ActorMaterializer requires an implicit ActorSystem
  implicit lazy val ec           = system.dispatcher
  implicit lazy val materializer = ActorMaterializer()

  def run(args: List[String]): IO[ExitCode] =
    for {
      processing <- ClassMyActorRef.apply()
      sqlactor   <- ClassMyActorRef.apply()
      streemdata <- MyStreemRef.apply()
      streemfrop <- MyStreemRef.apply()
      cort       = (processing, sqlactor, streemdata, streemfrop)
      cs         = IO.contextShift(ec)
      actorTast  = system.actorOf(ActorControllerSupervisor(cort))
      appRoute   = myRout(cort, actorTast)
      _          <- IO.fromFuture(IO(Http().bindAndHandle(appRoute, "0.0.0.0", 8080)))
    } yield ExitCode.Success
}
