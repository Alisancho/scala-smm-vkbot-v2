package ru.database.query

import ru.MyContext
import ru.database.quillmodels.ConditionSave

import scala.concurrent.ExecutionContextExecutor

trait QueryCondition {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def updateConditionAddOne(condition: ConditionSave) = ctx.run(
    query[ConditionSave]
      .filter(r => r.idtask == lift(condition.idtask))
      .update(
        z ⇒ z.current_day → (z.current_day + 1),
        _.datatime_latest_addition → lift(condition.datatime_latest_addition)
      )
  )
}
