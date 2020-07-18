package ru.database.query

import ru.MyContext
import ru.database.quillmodels.Proxyserver

import scala.concurrent.ExecutionContextExecutor

trait QueryProxyserver {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor
  import ctx._

  def updateStatusProxy(newProxy: Proxyserver) = ctx.run(
    query[Proxyserver]
      .filter(r => r.idproxyserver == lift(newProxy.idproxyserver))
      .update(_.status → lift(newProxy.status))
  )
}
