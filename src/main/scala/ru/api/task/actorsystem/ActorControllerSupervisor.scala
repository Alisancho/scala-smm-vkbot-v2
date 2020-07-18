package ru.api.task.actorsystem

import akka.Done
import akka.actor.{ActorRef, Kill, Props}
import akka.stream.{ActorMaterializer, KillSwitches, SharedKillSwitch}
import cats.effect.{ContextShift, IO}
import ru.AppStart.config
import ru.MyContext
import ru.api.format.ActorSystemTask
import ru.api.task.ref.{MyStrrmRef, _}
import ru.core.actors.tupetask.MainFunction
import ru.helper.MyActor

import scala.concurrent.{ExecutionContextExecutor, Future}

object ActorControllerSupervisor {
  def apply(cort: (ClassMyActorRef, ClassMyActorRef, MyStreemRef, MyStreemRef))(implicit ec: ExecutionContextExecutor,
                                                                                materializer: ActorMaterializer,
                                                                                ctx: MyContext,
                                                                                cs: ContextShift[IO]) =
    Props(new ActorControllerSupervisor(cort._1, cort._2, cort._3, cort._4))

  case class TaskForRef(task: String, obj: MainRef)
  lazy private val TEME_DUMP_STATUS = config.getString("TEME_DUMP_STATUS").toInt
  lazy private val TEME_SQL         = config.getString("TEME_SQL").toInt
}

/**
  *
  * @param processingRef
  * @param sqlRef
  * @param streemDataRef
  * @param streemDropRef
  * @param ec
  * @param materializer
  * @param ctx
  * @param cs
  */
class ActorControllerSupervisor(processingRef: ClassMyActorRef,
                                sqlRef: ClassMyActorRef,
                                streemDataRef: MyStreemRef,
                                streemDropRef: MyStreemRef)(implicit ec: ExecutionContextExecutor,
                                                            materializer: ActorMaterializer,
                                                            ctx: MyContext,
                                                            cs: ContextShift[IO])
    extends MyActor {
  import ActorControllerSupervisor.{TEME_DUMP_STATUS, TEME_SQL}
  override def receive: Receive = {

    /**
      * Стартуем процессинг
      */
    case ActorSystemTask("PROCESSING", "START") =>
      sender ! {
        (for {
          processingLocalRef <- processingRef.getRef
          sqlLocalRef        <- sqlRef.getRef
          result             = stertProcessing(processingLocalRef)
        } yield result).unsafeRunSync()
      }

    case ActorSystemTask("PROCESSING", "STOP") =>
      sender ! {
        (for {
          localRef <- processingRef.getRef
          result   = stopProcessing(localRef)
        } yield result).unsafeRunSync()
      }

    case a: ActorSystemTask
        if (a.actorName == "STREEMDATA" | a.actorName == "STREEMDROP") & (a.task == "START" | a.task == "STOP") =>
      sender ! infoStreem(a)
    case _ => sender ! "ERROR_MESS"

  }

  private def stertProcessing(infoTask: Option[ActorRef]) = infoTask match {
    case Some(value) => "PROCESSING_ALREDY_RUNNING"
    case None => {
      (for {
        processingLocalRef <- MainFunction.actorProcessing
        sqlLocalRef        <- MainFunction.actorSQL(processingLocalRef)
        _                  <- processingRef.setRef(processingLocalRef)
        _                  <- sqlRef.setRef(sqlLocalRef)
      } yield "SUCCESSES_PROCESSING_RUNNING").unsafeRunSync()
    }
  }

  private def stopProcessing(infoTask: Option[ActorRef]) = infoTask match {
    case Some(value) =>
      (for {
        _      <- IO.unit
        sqlref <- sqlRef.getRef
        q      <- streemDataRef.getRef
        w      <- streemDropRef.getRef
        _      <- IO { if (q.nonEmpty) { q.get.shutdown(); streemDataRef.setNone } }
        _      <- IO { if (w.nonEmpty) { w.get.shutdown(); streemDropRef.setNone } }
        _      <- IO { sqlref.get ! Kill }
        _      <- IO { value ! Kill }
        _      <- processingRef.setNone
        _      <- sqlRef.setNone
      } yield "SUCCESSES_PROCESSING_STOPPED").unsafeRunSync()

    case None => "PROCESSING_ALREDY_STOPPED"
  }

  private def infoStreem(actorSystemTask: ActorSystemTask): String = {
    (for {
      _        <- IO.unit
      localRef <- processingRef.getRef
      systemL = if (actorSystemTask.actorName == "STREEMDATA") (streemDataRef, MainFunction.new_data_gen(TEME_SQL, _, _))
      else (streemDropRef, MainFunction.dump_status(TEME_DUMP_STATUS, _, _))
      localRef2 <- systemL._1.getRef
      result    = streemComand((actorSystemTask.task, localRef.nonEmpty, localRef2.nonEmpty))(systemL._1)(systemL._2)
    } yield result).unsafeRunSync()
  }

  /**
    * true - работает
    * false - выключин
    *
    * @param cort
    * @param getStreem
    * @param myStreem
    * @return
    */
  private def streemComand(cort: (String, Boolean, Boolean))(myStreem: MyStrrmRef)(
      getStreem: (SharedKillSwitch, ActorRef) => IO[Future[Done]]) =
    cort match {
      case ("START", true, true)   => "STREEM_STARTED"
      case ("START", false, false) => "NEED_START_PROCESSING"

      case ("START", true, false) => {
        (for {
          sqlref <- sqlRef.getRef
          killer <- IO { KillSwitches.shared("my-kill-switch") }
          _      <- getStreem(killer, sqlref.get)
          _      <- myStreem.setRef(killer)
        } yield "SUCCESSES").unsafeRunSync()
      }

      case ("STOP", true, true) => {
        (for {
          p <- myStreem.getRef
          _ = p.get.shutdown()
          _ <- myStreem.setNone
        } yield "SUCCESSES").unsafeRunSync()
      }
      case ("STOP", _, false) => "STREEM_IS_OFF"
      case _                  => "ERROR_streemComand"
    }

}
