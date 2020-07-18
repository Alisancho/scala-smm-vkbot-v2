package ru.api.rout

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{complete, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import ru.MyContext
import ru.api.rout.v1.ApiV1Main
import ru.api.rout.v1.actor.ActorRout
import ru.api.task.ref.{ClassMyActorRef, MyStreemRef}
import ru.database.query.{QueryEnrichment, QueryWoman}
import ru.vk.VKAPI

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

trait MainAPI extends QueryEnrichment with VKAPI with QueryWoman with ApiV1Main with ActorRout {
  implicit val system: ActorSystem
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  def myRout(cort: (ClassMyActorRef, ClassMyActorRef, MyStreemRef, MyStreemRef), actorTast: ActorRef)(
      implicit cs: ContextShift[IO]): Route =
    path("status") {
      get {
        complete("SERWER_IN_WORK")
      }
    } ~ routApiV1(cort) ~ startActors(cort, actorTast)

}
