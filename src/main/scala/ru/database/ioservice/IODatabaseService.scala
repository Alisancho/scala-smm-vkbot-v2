package ru.database.ioservice

import ru.MyContext
import ru.core.actors.ActorProcessingSupervisor.MainTask
import ru.database.query.QueryFriendList

import scala.concurrent.ExecutionContextExecutor

object IODatabaseService {
  def apply(implicit
            ec: ExecutionContextExecutor,
            ctx: MyContext) = new IODatabaseService
}
class IODatabaseService(
    implicit
    val ec: ExecutionContextExecutor,
    val ctx: MyContext
) extends QueryFriendList {

  def getFriendList(mainTask: MainTask) = getInfoBlackList(mainTask.task.idtask, "EXPECTS", 60)

}
