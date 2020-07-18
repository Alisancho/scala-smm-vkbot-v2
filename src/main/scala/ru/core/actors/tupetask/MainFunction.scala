package ru.core.actors.tupetask

import java.time.Instant
import java.util.Date

import akka.Done
import akka.actor.ActorRef
import akka.event.LoggingAdapter
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, SharedKillSwitch, ThrottleMode}
import cats.effect.IO
import ru.AppStart.system
import ru.core.actors.ActorProcessingSupervisor
import ru.core.actors.ActorProcessingSupervisor.{MainTask, WomanWithProxy}
import ru.core.actors.ActorTask.NextWoman
import ru.core.actors.tupetask.friend.ActorSearchFriend.NextList
import ru.database.actors.ActorEnrichment
import ru.database.quillmodels.TrigerDump
import ru.futuretasks.IOTaskCleanFriend
import ru.http.thisclass.ClassProxyRest
import ru.vk.{VKAPI, VKAPIFriend}
import ru.{ConfigClass, MyContext}

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

object MainFunction extends VKAPI with VKAPIFriend {

  def actorProcessing(implicit ec: ExecutionContextExecutor, materializer: ActorMaterializer, ctx: MyContext): IO[ActorRef] =
    IO {
      system.actorOf(ActorProcessingSupervisor(), name = "ActorProcessingSupervisor")
    }

  def actorSQL(processing: ActorRef)(implicit ec: ExecutionContextExecutor,
                                     materializer: ActorMaterializer,
                                     ctx: MyContext): IO[ActorRef] =
    IO {
      system.actorOf(ActorEnrichment(processing), name = "actorSQL")
    }

  def new_data_gen(TEME_SQL: Int, sharedKillSwitch: SharedKillSwitch, SQLActor: ActorRef)(
      implicit materializer: ActorMaterializer): IO[Future[Done]] = IO {
    Source
      .tick(3.seconds, TEME_SQL.minutes, ())
      .via(sharedKillSwitch.flow)
      .runForeach { _ ⇒
        SQLActor ! new Date
      }
  }

  def dump_status(TEME_DUMP_STATUS: Int, sharedKillSwitch: SharedKillSwitch, SQLActor: ActorRef)(
      implicit materializer: ActorMaterializer): IO[Future[Done]] = IO {
    Source
      .tick(3.seconds, TEME_DUMP_STATUS.minutes, ())
      .via(sharedKillSwitch.flow)
      .runForeach { _ ⇒
        SQLActor ! TrigerDump(ConfigClass.trigerDump, Instant.now())
      }
  }

  /**
    * Метод ля получения случайного возраста
    *
    * @param mainTask главный таск
    * @return
    */
  def randomDelay(mainTask: MainTask): Int =
    Random.nextInt(mainTask.task.delay_max - mainTask.task.delay_min) + mainTask.task.delay_min

  /**
    *Метод делает телку онлайн
    *
    * @param access_token Токен
    * @param sharedKillSwitch киллер для остановки стрима
    * @param materializer materializer
    * @param classProxyRest HTTP клиент с прокси
    * @return
    */
  def onlineWoman(access_token: String, sharedKillSwitch: SharedKillSwitch)(implicit materializer: ActorMaterializer,
                                                                            classProxyRest: ClassProxyRest): Future[Done] =
    Source
      .tick(1.seconds, 240.seconds, ())
      .via(sharedKillSwitch.flow)
      .runForeach { _ =>
        classProxyRest.get(toOnline(access_token).toString())
      }

  /**
    * Метод для асинхронной обработки списка
    *
    * @param list список
    * @param getInfo Метод который возвращает Future со списком
    * @param getObj Метод
    * @param materializer materializer
    * @tparam R тип который должен вернуться
    * @return
    */
  def getFilterList[R](list: List[Int])(getInfo: R => Future[List[R]], getObj: Int => R)(
      implicit materializer: ActorMaterializer): Future[Seq[Any]] =
    Source(list)
      .mapAsyncUnordered(parallelism = 4)(p => getInfo(getObj(p)))
      .map(o => if (o.isEmpty) o.head else 0)
      .runWith(Sink.seq)

  /**
    *
    * @param id
    * @param localTask
    * @tparam R
    * @tparam I
    * @return
    */
  def getOneTask[R, I](id: I)(localTask: I => R): R = localTask(id)

  /**
    *
    * @param mainTask
    * @param womanWithProxy
    * @param sharedKillSwitch киллер для остановки стрима
    * @param newList основной лист для рассылки
    * @param actorTask адрес актора таска
    * @param actorParent актор для рассылки праглашения в друзья
    * @param randomLocal
    * @param funGetOneTask метод который возвращает IO с одним приглашенинм
    * @param materializer materializer
    * @param log логирование
    * @tparam TYPE_LIST ТИП листа
    * @return
    */
  def mainStreem[TYPE_LIST](mainTask: MainTask,
                            womanWithProxy: WomanWithProxy,
                            sharedKillSwitch: SharedKillSwitch,
                            newList: List[TYPE_LIST])(actorTask: ActorRef, actorParent: Option[ActorRef] = None)(
      randomLocal: MainTask => Int,
      funGetOneTask: TYPE_LIST => IO[Unit])(implicit
                                            materializer: ActorMaterializer,
                                            log: LoggingAdapter,
                                            ec: ExecutionContextExecutor): Future[Done] =
    Source(Seq.range(0, newList.size))
      .throttle(
        1,
        5.second,
        1,
        ThrottleMode.shaping
      )
      .flatMapConcat { а =>
        val seconds = randomLocal(mainTask)
        Source.single(а).delay(seconds.seconds)
      }
      .via(sharedKillSwitch.flow)
      .mapAsync(parallelism = 1)(k => {
        val lTask = funGetOneTask(newList(k)).unsafeToFuture()
        if (newList(k) == newList(newList.size - 1)) {
          actorParent match {
            case Some(value) => value ! NextList()
            case None        => actorTask ! NextWoman(womanWithProxy.woman.idwoman)
          }
        }
        lTask
      })
      .runForeach(g => ())

  def getCleanFriendStreem(list: scala.collection.immutable.Seq[Int],
                           accessToken: String)(sharedKillSwitch: SharedKillSwitch, IOTaskCleanFriend: IOTaskCleanFriend)(
      implicit
      materializer: ActorMaterializer): Future[Done] =
    Source(list)
      .throttle(
        1,
        2.second,
        1,
        ThrottleMode.shaping
      )
      .via(sharedKillSwitch.flow)
      .runForeach(i => IOTaskCleanFriend.getIOTask(ID_MAN = i, sharedKillSwitch).unsafeRunAsync(_ => ()))

}
