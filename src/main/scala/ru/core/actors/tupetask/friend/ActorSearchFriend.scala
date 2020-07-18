package ru.core.actors.tupetask.friend

import java.time.Instant

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import play.api.libs.json.Json
import ru.core.actors.ActorProcessingSupervisor.{MainTask, WomanWithProxy}
import ru.core.actors.ActorTask.NextWoman
import ru.database.query.{QueryBlackListFriend, QueryTaskWoman, QueryWoman}
import ru.database.quillmodels.TaskWoman
import ru.helper.logger.LoggerMain
import ru.helper.{MyActor, RandomServiceImpl}
import ru.http.thisclass.ClassProxyRest
import ru.vk.VKAPISearch
import ru.vk.response.JSONVKUsersSearch
import ru.vk.response.VKObject._
import ru.{ConfigClass, MyContext}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

object ActorSearchFriend {
  def apply(mainTask: MainTask, womanWithProxy: WomanWithProxy)(implicit system: ActorSystem,
                                                                ctx: MyContext,
                                                                ec: ExecutionContextExecutor,
                                                                materializer: ActorMaterializer): Props =
    Props(new ActorSearchFriend(mainTask, womanWithProxy)).withDispatcher(ConfigClass.dispatcher)
  case class NextList()
}

class ActorSearchFriend(val mainTask: MainTask, womanWithProxy: WomanWithProxy)(implicit val system: ActorSystem,
                                                                                val ctx: MyContext,
                                                                                val ec: ExecutionContextExecutor,
                                                                                materializer: ActorMaterializer)
    extends MyActor with VKAPISearch with QueryBlackListFriend with QueryTaskWoman with QueryWoman {
  private val nameActor = "ACTOR_SEARCH_FRIEND"
  import ActorSearchFriend._
  implicit val timeout: Timeout                       = Timeout(10 seconds)
  implicit private val classProxyRest: ClassProxyRest = new ClassProxyRest(womanWithProxy.proxy)
  lazy private val random                             = new RandomServiceImpl
  private var actorStreem: ActorRef                   = _
  private var num                                     = 0
  private val listMan                                 = getLisfBoy(womanWithProxy.woman.access_token)
  private var num2                                    = 0

//  val newFunGetBlack: Int => BlackListFriend = BlackListFriend(namelist = mainTask.task.blacklist,
//                                                               _,
//                                                               idwoman = womanWithProxy.woman.idwoman,
//                                                               datatime = Instant.now(),
//                                                               reason = "SEARCH")

  override def receive: Receive = {
    case _: NextList ⇒ {

      if (num != 0) context.stop(actorStreem)
      log.info("REQ " + getLisfBoy(womanWithProxy.woman.access_token, getRandomAge).toString)

      val requestList: Unit = classProxyRest
        .get(getLisfBoy(womanWithProxy.woman.access_token, getRandomAge).toString)
        .onComplete {

          case Success(value) => {
            if (value.contains("error_code")) {
              log.info("resp=" + value)
              log.error(s"TASK_ID=${mainTask.task.idtask} ERROR_SEARCH_FRIEND WOMAN_ID=${womanWithProxy.woman.idwoman} $value")
              updateWomanStatus(womanWithProxy.woman.copy(status = "ERROR_5"))
              context.parent ! NextWoman(womanWithProxy.woman.idwoman)
            } else {
              log.info("resp=" + value)
              val objectListMan = Json.parse(value).as[JSONVKUsersSearch]
              val oldList = objectListMan.response.items
                .map(e ⇒ e.id)
                .toList

              for {
                blistfromdb  <- checkListFriend(oldList, mainTask.task.blacklist)
                blistfromdb2 = blistfromdb.map(_.idman)
                newList      = oldList.filter(x => !blistfromdb2.contains(x))
                _            = log.info("LIST_NEW=" + newList)
              } yield nonEmptyList(newList)
            }
          }
          case Failure(exception) => {
            log.error(exception.toString)
            updateWomanStatus(womanWithProxy.woman.copy(status = "ERROR_PROXY_INITIALIZATION"))
            context.parent ! NextWoman(womanWithProxy.woman.idwoman)
          }
        }
    }
    case _ ⇒ log.error("ERROR_MESS")
  }

  private def getRandomAge: Int =
    mainTask.searchlist.get.age_from + Random.nextInt(
      mainTask.searchlist.get.age_to - mainTask.searchlist.get.age_from
    )
  private def nonEmptyList(newList: List[Int]) = {
    if (newList.nonEmpty) {
      actorStreem = context.actorOf(
        ActorStreemFriend(
          mainTask,
          womanWithProxy,
          objectListMan = newList,
        )(actorTask = context.parent),
        name = "STREEM_FRIEND_" + mainTask.task.idtask + random.actorID
      )
      num += 1
    } else {
      if (num2 == 10) {
        context.parent ! NextWoman(womanWithProxy.woman.idwoman)
      } else {
        num2 += 1
        context.self ! NextList()
      }
    }
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
//    context.stop(actorStreem)
    LoggerMain.stopActorLogger(nameActor, mainTask, Option.apply(womanWithProxy.woman))
  }
  override def supervisorStrategy = OneForOneStrategy() {
    case e => {
      context.stop(actorStreem)
      context.parent ! NextWoman(womanWithProxy.woman.idwoman)
      log.error("ERROR_UNKNOW_STREEM=" + e)
      akka.actor.SupervisorStrategy.Stop
    }
  }

}
