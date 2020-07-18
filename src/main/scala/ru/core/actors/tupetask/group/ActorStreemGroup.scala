package ru.core.actors.tupetask.group

import java.time.Instant

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, KillSwitches}
import cats.effect.IO
import play.api.libs.json.Json
import ru.core.actors.ActorProcessingSupervisor.{MainTask, WomanWithProxy}
import ru.core.actors.ActorTask.NextWoman
import ru.core.actors.tupetask.MainFunction
import ru.core.actors.tupetask.group.ActorStreemGroup.StastGroup
import ru.database.Serially
import ru.database.query.{QueryBlackListGroup, QueryTaskWoman, QueryWoman}
import ru.database.quillmodels.TaskWoman
import ru.futuretasks.IOTaskGroup
import ru.helper.MyActor
import ru.helper.logger.LoggerMain
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPI
import ru.vk.response.JSONVKFriendsGet
import ru.vk.response.VKObject._
import ru.{ConfigClass, MyContext}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object ActorStreemGroup {
  def apply(
      mainTask: MainTask,
      womanWithProxy: WomanWithProxy
  )(actorTask: ActorRef)(
      implicit materializer: ActorMaterializer,
      ec: ExecutionContextExecutor,
      system: ActorSystem,
      ctx: MyContext,
  ): Props = Props(new ActorStreemGroup(mainTask, womanWithProxy)(actorTask)).withDispatcher(ConfigClass.dispatcher)
  case class StastGroup()
}

class ActorStreemGroup(val mainTask: MainTask, womanWithProxy: WomanWithProxy)(actorTask: ActorRef)(
    implicit val materializer: ActorMaterializer,
    val ec: ExecutionContextExecutor,
    val system: ActorSystem,
    val ctx: MyContext,
) extends MyActor with VKAPI with QueryBlackListGroup with QueryTaskWoman with QueryWoman with Serially {
  private val nameActor                            = "ACTOR_STREEM_GROUP"
  private lazy val sharedKillSwitch                = KillSwitches.shared("my-kill-switch")
  implicit lazy val classProxyRest: ClassProxyRest = ClassProxyRest(womanWithProxy.proxy)
  private val IOTask = IOTaskGroup(
    mainTask.task.blacklist,
    womanWithProxy.woman,
    mainTask
  )(context.parent)

  private def newList(requestList: String): IO[List[Int]] = requestList match {
    case a: String if a.contains("error_code") =>
      IO {
        updateWomanStatus(womanWithProxy.woman.copy(status = "ERROR_5"))
        log.error("ERROR_GET_LIST_FRIEND" + a)
        val lo: List[Int] = Nil
        lo
      }
    case d: String =>
      for {
        oldList <- IO {
                    Json.parse(d).as[JSONVKFriendsGet].response.items.toList
                  }
        blistfromdb  <- IO.fromFuture(IO(checkListGroup(oldList, mainTask.task.blacklist)))
        blistfromdb2 = blistfromdb.map(_.idman)
        newList      = oldList.filter(x => !blistfromdb2.contains(x))
      } yield newList
  }

  override def receive: Receive = {
    case _: StastGroup => {

      LoggerMain.httpRequest(
        mainTask,
        womanWithProxy.woman,
        getFriends(ID_USER = womanWithProxy.woman.idwoman, access_token = womanWithProxy.woman.access_token).toString)

      val urL = getFriends(ID_USER = womanWithProxy.woman.idwoman, access_token = womanWithProxy.woman.access_token).toString

      (for {
        req     <- IO.fromFuture(IO(classProxyRest.get(urL)))
        _       <- IO { LoggerMain.httpResponse(mainTask, womanWithProxy.woman, req) }
        newList <- newList(req)
        _       <- IO { LoggerMain.newListLogger(mainTask, womanWithProxy.woman, newList) }
        z <- if (newList.isEmpty) IO(context.parent ! NextWoman(womanWithProxy.woman.idwoman))
            else
              IO {
                val streem = MainFunction.mainStreem(mainTask, womanWithProxy, sharedKillSwitch, newList)(
                  actorTask = context.parent)(MainFunction.randomDelay, IOTask.getIOTask)
                val online = MainFunction.onlineWoman(womanWithProxy.woman.access_token, sharedKillSwitch)
                (streem, online)
              }
      } yield z).unsafeToFuture().onComplete {
        case Success(value)     => {}
        case Failure(exception) => context.parent ! NextWoman(womanWithProxy.woman.idwoman)
      }
    }
    case _ ⇒ log.info("ERROR_MESS")
  }

  override def preStart(): Unit = {
    updateTaskWomanStartTime(
      TaskWoman(
        idtask = mainTask.task.idtask,
        status = "IN_WORK",
        idwoman = womanWithProxy.woman.idwoman,
        current_day = 1,
        datatime_start = Instant.now(),
        datatime_latest_addition = Instant.now(),
        datatime_stop = Instant.now()
      ))
    LoggerMain.startActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
  }

  override def postStop(): Unit = {
    updateTaskWomanStopTime(
      TaskWoman(
        idtask = mainTask.task.idtask,
        status = "COMPLETED_SUCCESSFULL",
        idwoman = womanWithProxy.woman.idwoman,
        current_day = 1,
        datatime_start = Instant.now(),
        datatime_latest_addition = Instant.now(),
        datatime_stop = Instant.now()
      ))
    sharedKillSwitch.shutdown()
    LoggerMain.stopActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
  }
}
