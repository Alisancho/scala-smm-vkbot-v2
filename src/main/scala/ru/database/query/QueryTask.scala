package ru.database.query

import ru.MyContext
import ru.database.quillmodels.Task

import scala.concurrent.ExecutionContextExecutor

trait QueryTask {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._
  def selectTaskForEVEN_DAYS(time: String) = {
    val rawQuery = quote { (time_new: String) =>
      infix"""SELECT idtask FROM task WHERE time_start <= $time_new and statustask = 'WAITING_FOR_EXECUTION' and (type_of_mailing = 'EVERY_DAY' OR type_of_mailing = 'EVEN_DAYS')"""
        .as[Query[(String)]]
    }
    ctx.run(rawQuery(lift(time)))
  }

  def selectTaskForODD_DAYS(time: String) = {
    val rawQuery = quote { (time_new: String) =>
      infix"""SELECT idtask FROM task WHERE time_start <= $time_new and statustask = 'WAITING_FOR_EXECUTION' and (type_of_mailing = 'EVERY_DAY' OR type_of_mailing = 'ODD_DAYS')"""
        .as[Query[(String)]]
    }
    ctx.run(rawQuery(lift(time)))
  }
  def updateStatusTask(task: Task) = ctx.run(
    query[Task]
      .filter(r => r.idtask == lift(task.idtask))
      .update(_.statustask → lift(task.statustask), _.timeupdate → lift(task.timeupdate))
  )
  def updateTaskStatus(task: Task) = ctx.run(
    query[Task]
      .filter(r => r.idtask == lift(task.idtask))
      .update(_.statustask → lift(task.statustask), _.timeupdate → lift(task.timeupdate))
  )
  def getTask(idTask: String) = ctx.run(
    quote {
      for {
        t <- query[Task] if t.idtask == lift(idTask)
      } yield t
    }
  )
}
