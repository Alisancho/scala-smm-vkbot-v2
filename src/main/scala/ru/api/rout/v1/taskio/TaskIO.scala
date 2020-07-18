package ru.api.rout.v1.taskio

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, KillSwitches}
import cats.effect.IO
import play.api.libs.json.Json
import ru.MyContext
import ru.api.format.CreateTask
import ru.core.actors.tupetask.MainFunction
import ru.database.query.{QueryEnrichment, QueryWoman}
import ru.futuretasks.IOTaskCleanFriend
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPI
import ru.vk.response.JSONVKFriendsGet

import scala.concurrent.ExecutionContextExecutor

trait TaskIO extends QueryWoman with QueryEnrichment with VKAPI {
  import ru.api.format.MyAPI._
  import ru.vk.response.VKObject._
  implicit val system: ActorSystem
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  def addTaskCleanWoman(): Route = {
    path("createTask") {
      post {
        entity(as[String]) { p =>
          val z = Json.parse(p).as[CreateTask]
          if (z.typeTask == "CLEAN") {
            (for {
              www            <- IO.fromFuture(IO(getWomanOne(z.woman)))
              v              <- IO.fromFuture(IO(getWomanWithProxyClean(www.head)))
              classProxyRest <- IO(ClassProxyRest(v.head._2))
              l              <- IO.fromFuture(IO(classProxyRest.get(getFriends(v.head._1.idwoman, v.head._1.access_token).toString())))
              k              <- IO(Json.parse(l).as[JSONVKFriendsGet].response.items)
              myIO           = IOTaskCleanFriend(classProxyRest, v.head._1)
              killer         = KillSwitches.shared("my-kill-switch")
              n              = MainFunction.getCleanFriendStreem(k, v.head._1.access_token)(killer, myIO)
            } yield n).unsafeRunAsync(_ => ())
            complete(s"START_WOMAN=${z.woman}")
          } else {
            complete(s"NO_TASK_${z.typeTask}")
          }
        }
      }
    }
  }
}
