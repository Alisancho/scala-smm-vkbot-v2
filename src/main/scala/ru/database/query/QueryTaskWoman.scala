package ru.database.query

import ru.MyContext
import ru.database.quillmodels.TaskWoman

import scala.concurrent.ExecutionContextExecutor

trait QueryTaskWoman {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def updateTaskWomanAddOne(taskWoman: TaskWoman) = ctx.run(
    query[TaskWoman]
      .filter(
        r =>
          r.idtask == lift(taskWoman.idtask) && r.idwoman == lift(
            taskWoman.idwoman
        )
      )
      .update(
        z ⇒ z.current_day → (z.current_day + 1),
        _.datatime_latest_addition → lift(taskWoman.datatime_latest_addition)
      )
  )

  def updateTaskWomanStartTime(taskWoman: TaskWoman) = ctx.run(
    query[TaskWoman]
      .filter(
        r =>
          r.idtask == lift(taskWoman.idtask) && r.idwoman == lift(
            taskWoman.idwoman
        )
      )
      .update(
        _.datatime_start → lift(taskWoman.datatime_start),
        _.status         → lift(taskWoman.status)
      )
  )

  def updateTaskWomanStopTime(taskWoman: TaskWoman) = ctx.run(
    query[TaskWoman]
      .filter(
        r =>
          r.idtask == lift(taskWoman.idtask) && r.idwoman == lift(
            taskWoman.idwoman
        )
      )
      .update(
        _.datatime_stop → lift(taskWoman.datatime_stop),
        _.status        → lift(taskWoman.status)
      )
  )
}
