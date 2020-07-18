package ru.database.query

import ru.MyContext
import ru.database.quillmodels.BlackListFriend

import scala.concurrent.ExecutionContextExecutor

trait QueryBlackListFriend {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor

  import ctx._

  def getInfoBlackList(blackFriend: BlackListFriend) = ctx.run(
    query[BlackListFriend]
      .filter(
        c =>
          c.namelist == lift(blackFriend.namelist) &&
            c.idman == lift(blackFriend.idman)
      )
  )

  def setBlackListFriend(blackFriend: BlackListFriend) = ctx.run(
    query[BlackListFriend].insert(
      _.namelist -> lift(blackFriend.namelist),
      _.idman    -> lift(blackFriend.idman),
      _.idwoman  -> lift(blackFriend.idwoman),
      _.datatime -> lift(blackFriend.datatime),
      _.reason   -> lift(blackFriend.reason)
    )
  )

  def checkListFriend(list: List[Int], namelist: String) =
    ctx.run(quote(query[BlackListFriend].filter(bl => liftQuery(list).contains(bl.idman) && bl.namelist == lift(namelist))))

}
