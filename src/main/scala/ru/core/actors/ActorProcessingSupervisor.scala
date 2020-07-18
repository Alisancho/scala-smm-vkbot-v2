package ru.core.actors
import akka.actor.{ActorSystem, OneForOneStrategy, Props, Terminated}
import akka.stream.ActorMaterializer
import cats.effect.IO
import ru.core.actors.ActorTask.StartTask
import ru.database.quillmodels.{Proxyserver, Searchoption, Task, Woman}
import ru.helper.MyActor
import ru.{ConfigClass, MyContext}

import scala.concurrent.ExecutionContextExecutor

object ActorProcessingSupervisor {
  def apply()(implicit system: ActorSystem,
              ctx: MyContext,
              ec: ExecutionContextExecutor,
              materializer: ActorMaterializer): Props =
    Props(new ActorProcessingSupervisor()).withDispatcher(ConfigClass.dispatcher)

  case class MainTask(task: Task, listWoman: List[WomanWithProxy], searchlist: Option[Searchoption] = None)

  case class WomanWithProxy(woman: Woman, proxy: Proxyserver)

  case class FinishTask(name: String)
}

class ActorProcessingSupervisor()(
    implicit val system: ActorSystem,
    ctx: MyContext,
    ec: ExecutionContextExecutor,
    materializer: ActorMaterializer
) extends MyActor {

  import ActorProcessingSupervisor._

  override def receive: Receive = {

    case t: MainTask =>
      (for {
        cd <- IO { context.actorOf(ActorTask(t), name = "TASK_FRIEND=" + t.task.idtask) }
        _  <- IO { cd ! StartTask() }
      } yield ()).unsafeRunAsync(_ => ())

    case Terminated(subscriber) => { log.warning("Terminated=" + subscriber.toString()) }
    case _                      => log.error("ERROR_MESS")
  }

  override def preStart(): Unit = {
    log.info("START=ACTOR_PROCESSING")
  }

  override def postStop(): Unit = {
    log.info("STOP=ACTOR_PROCESSING")
  }
  override def supervisorStrategy = OneForOneStrategy() {
    case e => {
      log.error("ERROR_IN_ACTORTASK=" + e)
      System.gc()
      akka.actor.SupervisorStrategy.Stop
    }
  }
//
//  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
//    super.preRestart(reason, message)
//    log.info("RESTART=ActorProcessingSupervisor")
//  }
}
