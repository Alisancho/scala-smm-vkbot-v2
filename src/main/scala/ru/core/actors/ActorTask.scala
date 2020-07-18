package ru.core.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import cats.effect.IO
import ru.core.actors.ActorProcessingSupervisor.MainTask
import ru.core.actors.ActorTask.{NextWoman, NoList, PlusOne, StartTask}
import ru.core.actors.tupetask.friend.ActorSearchFriend
import ru.core.actors.tupetask.friend.ActorSearchFriend.NextList
import ru.core.actors.tupetask.friendlist.ActorStreemFriendList
import ru.core.actors.tupetask.group.ActorStreemGroup
import ru.core.actors.tupetask.group.ActorStreemGroup.StastGroup
import ru.database.query.{QueryBlackListFriend, QueryTask, QueryTaskWoman, QueryWoman}
import ru.helper.logger.LoggerMain
import ru.helper.{MyActor, RandomServiceImpl}
import ru.vk.VKAPISearch
import ru.{ConfigClass, MyContext}

import scala.concurrent.ExecutionContextExecutor

object ActorTask {
  case class StartTask()

  case class NextWoman(idWoman: Int)

  case class NoList()

  case class PlusOne(woman: Int)

  def apply(mainTask: MainTask)(implicit system: ActorSystem,
                                ctx: MyContext,
                                ec: ExecutionContextExecutor,
                                materializer: ActorMaterializer): Props =
    Props(new ActorTask(mainTask)).withDispatcher(ConfigClass.dispatcher)

}

class ActorTask(val mainTask: MainTask)(implicit val system: ActorSystem,
                                        val ctx: MyContext,
                                        val ec: ExecutionContextExecutor,
                                        materializer: ActorMaterializer)
    extends MyActor with VKAPISearch with QueryBlackListFriend with QueryTaskWoman with QueryWoman with QueryTask {
  private val nameActor    = "ACTOR_TASK"
  private var statusFin    = "COMPLETED_SUCCESSFULLY"
  var actorWoman: ActorRef = _
  var friend               = 0
  val random               = new RandomServiceImpl
  var num: Int             = -1

  override def receive: Receive = {
    case w: NextWoman ⇒ {
      if (w.idWoman == mainTask.listWoman(num).woman.idwoman) {
        friend = 0
        context.stop(actorWoman)
        num += 1
        if (num < mainTask.listWoman.size) {
          LoggerMain.nextWoman(mainTask, mainTask.listWoman(num).woman)
          startSystemL
        } else {
          context.stop(context.self)
        }
      } else {
        LoggerMain.oldWoman(mainTask, w.idWoman)
      }
    }
    case _: StartTask => {
      num += 1
      if (num < mainTask.listWoman.size) {
        LoggerMain.nextWoman(mainTask, mainTask.listWoman(num).woman)
        startSystemL
      } else {
        context.stop(context.self)
      }
    }
    case _: NoList => {
      statusFin = "NO_ELEMENT"
      context.stop(context.self)
    }
    case e: PlusOne ⇒ {
      if (e.woman == mainTask.listWoman(num).woman.idwoman) {
        friend += 1
        if (friend >= mainTask.searchlist.get.max_add_friend_man) {
          context.self ! NextWoman(mainTask.listWoman(num).woman.idwoman)
        }
      }

    }
    case _ ⇒ log.warning("ERROR_MESS")
  }

  private def getChild =
    mainTask.task.type_task match {
      case "ADD_FRIEND" =>
        context.actorOf(
          ActorSearchFriend(
            mainTask = mainTask,
            womanWithProxy = mainTask.listWoman(num)
          ),
          name = "SEARCH_FRIEND_" + mainTask.task.idtask + "_" + random.actorID
        )
      case "INVITE_GROUP" =>
        context.actorOf(
          ActorStreemGroup(
            mainTask = mainTask,
            womanWithProxy = mainTask.listWoman(num)
          )(actorTask = context.self),
          name = "SEARCH_FRIEND_" + mainTask.task.idtask + "_" + random.actorID
        )
      case "ADD_FRIEND_FOR_LIST" => context.actorOf(ActorStreemFriendList(mainTask, mainTask.listWoman(num)))
    }

  private def startSystemL: Unit = mainTask.task.type_task match {
    case "ADD_FRIEND" => {
      actorWoman = getChild
      actorWoman ! NextList()
    }
    case "INVITE_GROUP" => {
      actorWoman = getChild
      actorWoman ! StastGroup()
    }
    case "ADD_FRIEND_FOR_LIST" => actorWoman = getChild
  }

  override def preStart(): Unit = {
    LoggerMain.startActorLogger(nameActor, mainTask)
  }

  override def postStop(): Unit = {
    (for {
      _ <- IO.fromFuture(IO(updateTaskStatus(mainTask.task.copy(statustask = "COMPLETED_SUCCESSFULLY"))))
    } yield LoggerMain.stopActorLogger(nameActor, mainTask)).unsafeRunSync()
  }
}
