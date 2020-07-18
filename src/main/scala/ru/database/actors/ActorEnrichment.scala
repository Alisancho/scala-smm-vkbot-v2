package ru.database.actors

import java.util.Date

import akka.actor.{ActorRef, Props}
import cats.effect.IO
import ru.core.actors.ActorProcessingSupervisor.{MainTask, WomanWithProxy}
import ru.database.helper.EnrichmentHelper
import ru.database.query.{QueryEnrichment, QuerySearchOption, QueryTask}
import ru.database.quillmodels._
import ru.helper.MyActor
import ru.{ConfigClass, MyContext}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object ActorEnrichment {
  def apply(mainSpider: ActorRef)(
      implicit ctx: MyContext,
      ec: ExecutionContextExecutor
  ): Props = Props(new ActorEnrichment(mainSpider)).withDispatcher(ConfigClass.dispatcher)
}

class ActorEnrichment(mainSpider: ActorRef)(
    implicit val ctx: MyContext,
    val ec: ExecutionContextExecutor
) extends MyActor with EnrichmentHelper with QueryEnrichment with QueryTask with QuerySearchOption {
  def receive: Receive = {
    case newTime: Date ⇒ {
      val tasksS = parser(output(newTime)).toString match {
        case "EVEN_DAYS" ⇒
          selectTaskForEVEN_DAYS(dateFormatTime.format(newTime))
        case "ODD_DAYS" ⇒ selectTaskForODD_DAYS(dateFormatTime.format(newTime))
      }
      tasksS.onComplete {
        case Success(value) => {
          // Runtime.getRuntime.gc()
          for (p <- 0 to value.size - 1) {
            getTask(value(p)).foreach(localTask => {
              if (localTask.head.type_task == "ADD_FRIEND") {
                for {
                  _             <- updateStatusTask(localTask.head.copy(statustask = "IN_PROGRESS"))
                  searsh        <- getSearchOption(localTask.head.idtask).map(_.map(i => i).head)
                  womanWithProx <- getWomanWithProxy(localTask.head.idtask).map(_.map((WomanWithProxy.apply _).tupled))
                  mainTask      = MainTask(localTask.head, womanWithProx, Option.apply(searsh))
                } yield searchWomanInTask(mainTask).unsafeRunAsync(_ => ())
              } else if (localTask.head.type_task == "INVITE_GROUP" || localTask.head.type_task == "ADD_FRIEND_FOR_LIST") {
                for {
                  _             <- updateStatusTask(localTask.head.copy(statustask = "IN_PROGRESS"))
                  womanWithProx <- getWomanWithProxy(localTask.head.idtask).map(_.map((WomanWithProxy.apply _).tupled))
                  mainTask      = MainTask(localTask.head, womanWithProx)
                } yield searchWomanInTask(mainTask).unsafeRunAsync(_ => ())
              }
            })
          }
        }
        case Failure(exception) => {
          log.error(exception.toString)
        }
      }
    }
    case t: TrigerDump ⇒ updateTriger(t)
    case _             ⇒ log.error("NO_INFO")
  }
  private def searchWomanInTask(mainTask: MainTask): IO[Unit] = IO {
    if (mainTask.listWoman.isEmpty) {
      updateStatusTask(mainTask.task.copy(statustask = "NO_WOMAN"))
    } else {
      mainSpider ! mainTask
    }
  }
  override def preStart(): Unit = {
    log.info("START=ACTOR__SQL")
  }

  override def postStop(): Unit = {
    log.info("STOP=ACTOR__SQL")
  }
}
