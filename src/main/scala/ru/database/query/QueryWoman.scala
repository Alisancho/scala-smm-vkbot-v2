package ru.database.query

import ru.MyContext
import ru.database.quillmodels.Woman

import scala.concurrent.ExecutionContextExecutor

trait QueryWoman {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def updateWomanStatus(woman: Woman) = ctx.run(
    query[Woman]
      .filter(
        r => r.idwoman == lift(woman.idwoman)
      )
      .update(
        _.status → lift(woman.status),
      )
  )
  def getWomanOne(woman: Int) = ctx.run(
    query[Woman]
      .filter(
        r => r.idwoman == lift(woman)
      )
  )
}
