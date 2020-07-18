package ru.api.rout.v1.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cats.effect.{ContextShift, IO}
import play.api.libs.json.Json
import ru.MyContext
import ru.api.format.ActorSystemTask
import ru.api.task.ref.{ClassMyActorRef, MyStreemRef}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

trait ActorRout {
  implicit val system: ActorSystem
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout = Timeout(60.seconds)
  import ru.api.format.MyAPI._
  def startActors(cort: (ClassMyActorRef, ClassMyActorRef, MyStreemRef, MyStreemRef), actorTast: ActorRef)(
      implicit cs: ContextShift[IO]): Route = {
    pathPrefix("server") {
      path("actorSystem") {
        post {
          entity(as[String]) { p =>
            val op = Future {
              for {
                _      <- Future.unit
                myJson = Json.parse(p).as[ActorSystemTask]
                k      <- actorTast.ask(myJson).mapTo[String]
              } yield k
            }
            complete(op)
          }
        }
      }
    }
  }

}
