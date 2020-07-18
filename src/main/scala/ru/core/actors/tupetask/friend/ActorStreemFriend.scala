package ru.core.actors.tupetask.friend

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, KillSwitches}
import ru.core.actors.ActorProcessingSupervisor.{MainTask, WomanWithProxy}
import ru.core.actors.tupetask.MainFunction
import ru.futuretasks.IOTaskFriend
import ru.helper.MyActor
import ru.helper.logger.LoggerMain
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPI
import ru.{ConfigClass, MyContext}

import scala.concurrent.{ExecutionContextExecutor, Future}

object ActorStreemFriend {
  def apply(mainTask: MainTask, womanWithProxy: WomanWithProxy, objectListMan: List[Int])(actorTask: ActorRef)(
      implicit materializer: ActorMaterializer,
      ec: ExecutionContextExecutor,
      system: ActorSystem,
      ctx: MyContext,
      classProxyRest: ClassProxyRest
  ): Props =
    Props(new ActorStreemFriend(mainTask, womanWithProxy, objectListMan)(actorTask))
      .withDispatcher(ConfigClass.dispatcher)
  case class StartStreem()
}

class ActorStreemFriend(
    val mainTask: MainTask,
    womanWithProxy: WomanWithProxy,
    objectListMan: List[Int]
)(actorTask: ActorRef)(
    implicit materializer: ActorMaterializer,
    val ec: ExecutionContextExecutor,
    val system: ActorSystem,
    ctx: MyContext,
    classProxyRest: ClassProxyRest
) extends MyActor with VKAPI {
  private val nameActor        = "ACTOR_STREEM_FRIEND"
  private val sharedKillSwitch = KillSwitches.shared("my-kill-switch")
  private val myIO             = IOTaskFriend(classProxyRest, mainTask.task.blacklist, womanWithProxy.woman, mainTask)(actorTask: ActorRef)

  def receive: Receive = {
    case _ ⇒ log.error("ERROR_MESS")
  }
  private val mas =
    for {
      _ <- Future.unit
      streem = MainFunction.mainStreem(mainTask, womanWithProxy, sharedKillSwitch, objectListMan)(
        actorTask = actorTask,
        actorParent = Option.apply(context.parent))(MainFunction.randomDelay, myIO.getIOTask)
      online = MainFunction.onlineWoman(womanWithProxy.woman.access_token, sharedKillSwitch)
    } yield (online, streem)
  override def preStart(): Unit = {
    LoggerMain.startActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
  }

  override def postStop(): Unit = {
    sharedKillSwitch.shutdown()
    LoggerMain.stopActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
  }
}
