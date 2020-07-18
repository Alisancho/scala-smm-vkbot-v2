package ru.database.query

import ru.MyContext
import ru.database.quillmodels.Searchoption

import scala.concurrent.ExecutionContextExecutor

trait QuerySearchOption {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def getSearchOption(idTask: String) = ctx.run(
    quote {
      for {
        t <- query[Searchoption] if t.idtask == lift(idTask)
      } yield t
    }
  )
}
