package ru.database.query
import ru.MyContext
import ru.database.quillmodels.{TrigerDump, _}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait QueryEnrichment {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def updateTriger(triget_dump: TrigerDump) = ctx.run(
    query[TrigerDump]
      .filter(z ⇒ z.id == lift(triget_dump.id))
      .update(_.triger_data → lift(triget_dump.triger_data))
  )

  def getWomanWithProxy(idTask: String): Future[List[(Woman, Proxyserver)]] = ctx.run(
    quote {
      for {
        tw <- query[TaskWoman] if tw.idtask == lift(idTask)
        w  <- query[Woman] if w.idwoman == tw.idwoman && w.status == "ACTIVE"
        wp <- query[WomanProxy] if wp.idwoman == w.idwoman
        p  <- query[Proxyserver] if p.idproxyserver == wp.idproxyserver
      } yield (w, p)
    }
  )

  def getWomanWithProxyClean(womanLocale: Woman): Future[List[(Woman, Proxyserver)]] = ctx.run(
    quote {
      for {
        w  <- query[Woman] if w.idwoman == lift(womanLocale.idwoman)
        wp <- query[WomanProxy] if wp.idwoman == w.idwoman
        p  <- query[Proxyserver] if p.idproxyserver == wp.idproxyserver

      } yield (w, p)
    }
  )
}
