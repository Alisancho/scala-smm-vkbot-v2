package ru.database.query

import ru.MyContext
import ru.database.quillmodels.FriendList

import scala.concurrent.ExecutionContextExecutor

trait QueryFriendList {
  implicit val ctx: MyContext
  implicit val ec: ExecutionContextExecutor
  import ctx._

  def getInfoBlackList(idTask: String, status: String, limit: Int) = ctx.run(
    query[FriendList]
      .filter(
        c => c.idtask == lift(idTask) && c.status == lift(status)
      )
      .take(lift(limit))
  )

  def updateFriendListStatus(friendList: FriendList) = ctx.run(
    query[FriendList]
      .filter(
        r =>
          r.idtask == lift(friendList.idtask) && r.id_man == lift(
            friendList.id_man
        )
      )
      .update(
        _.status        → lift(friendList.status),
        _.data_time_add → lift(friendList.data_time_add),
        _.id_woman      → lift(friendList.id_woman)
      )
  )

}
