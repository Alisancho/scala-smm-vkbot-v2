package ru.core.actors.tupetask.friendlist

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, KillSwitches}
import ru.core.actors.ActorProcessingSupervisor.{MainTask, WomanWithProxy}
import ru.core.actors.ActorTask.NoList
import ru.core.actors.tupetask.MainFunction
import ru.database.query.QueryFriendList
import ru.futuretasks.IOTaskFriendList
import ru.helper.MyActor
import ru.helper.logger.LoggerMain
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPI
import ru.{ConfigClass, MyContext}

import scala.concurrent.{ExecutionContextExecutor, Future}

object ActorStreemFriendList {
  def apply(mainTask: MainTask, womanWithProxy: WomanWithProxy)(implicit materializer: ActorMaterializer,
                                                                ec: ExecutionContextExecutor,
                                                                system: ActorSystem,
                                                                ctx: MyContext): Props =
    Props(new ActorStreemFriendList(mainTask, womanWithProxy)(materializer, ec, system, ctx))
      .withDispatcher(ConfigClass.dispatcher)
}

class ActorStreemFriendList(val mainTask: MainTask, womanWithProxy: WomanWithProxy)(
    implicit materializer: ActorMaterializer,
    val ec: ExecutionContextExecutor,
    val system: ActorSystem,
    val ctx: MyContext
) extends MyActor with VKAPI with QueryFriendList {
  private val nameActor               = "ACTOR_STREEM_FRIEND_LIST"
  private val sharedKillSwitch        = KillSwitches.shared("my-kill-switch")
  implicit private val classProxyRest = ClassProxyRest(womanWithProxy.proxy)

  private val IOTask = IOTaskFriendList(
    womanWithProxy.woman,
    mainTask
  )(context.parent)

  def receive: Receive = {
    case _ ⇒ log.error("ERROR_MESS")
  }

  private val po = for {
    u          <- getInfoBlackList(mainTask.task.idtask, "EXPECTS", 60)
    logNewList = LoggerMain.newListLogger(mainTask, womanWithProxy.woman, u)
    online     = MainFunction.onlineWoman(womanWithProxy.woman.access_token, sharedKillSwitch)
    streem = if (u.isEmpty) Future { context.parent ! NoList() } else
      MainFunction.mainStreem(mainTask, womanWithProxy, sharedKillSwitch, u)(actorTask = context.parent)(
        MainFunction.randomDelay,
        IOTask.getIOTask)

  } yield (streem, online)

  override def preStart(): Unit = {
    LoggerMain.startActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
  }

  override def postStop(): Unit = {
    LoggerMain.stopActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
    sharedKillSwitch.shutdown()
  }
}
