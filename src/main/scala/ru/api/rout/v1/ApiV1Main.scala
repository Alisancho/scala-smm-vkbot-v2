package ru.api.rout.v1

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import ru.MyContext
import ru.api.rout.v1.taskio.TaskIO
import ru.api.task.ref.{ClassMyActorRef, MyStreemRef}

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

trait ApiV1Main extends TaskIO {

  implicit val system: ActorSystem
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def routApiV1(cort: (ClassMyActorRef, ClassMyActorRef, MyStreemRef, MyStreemRef))(implicit cs: ContextShift[IO]): Route =
    pathPrefix("api" / "v1") {
      path("version") {
        get {
          complete("version=2.4")
        }
      } ~ path("mainForm") {
        get {
          getFromResource("htmlres/index.html")
        }
      } ~ addTaskCleanWoman() ~ path("STOP_SERVER") {
        get {
          sys.exit(1)
          complete("wd")
        }
      }
    }
}
