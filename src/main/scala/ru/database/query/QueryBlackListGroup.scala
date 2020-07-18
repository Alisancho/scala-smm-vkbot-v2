package ru.database.query
import ru.MyContext
import ru.database.quillmodels.{BlackListFriend, BlackListGroup}

import scala.concurrent.ExecutionContextExecutor

trait QueryBlackListGroup {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def getInfoBlackList(blackGroup: BlackListGroup) = ctx.run(
    query[BlackListGroup]
      .filter(
        c =>
          c.idgroup == lift(blackGroup.idgroup) &&
            c.idman == lift(blackGroup.idman)
      )
  )

  def setBlackListGroup(blackGroup: BlackListGroup) = ctx.run(
    query[BlackListGroup].insert(
      _.idgroup  -> lift(blackGroup.idgroup),
      _.idman    -> lift(blackGroup.idman),
      _.idwoman  -> lift(blackGroup.idwoman),
      _.datatime -> lift(blackGroup.datatime),
      _.reason   -> lift(blackGroup.reason)
    )
  )

  def checkListGroup(list: List[Int], idGroup: String) =
    ctx.run(quote(query[BlackListGroup].filter(bl => liftQuery(list).contains(bl.idman) && bl.idgroup == lift(idGroup))))
}
