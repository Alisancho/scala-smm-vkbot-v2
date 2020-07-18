package ru.database.worker
import ru.database.query.QueryEnrichment
import ru.MyContext
import ru.database.query.{QueryBlackListFriend, QueryBlackListGroup}

import scala.concurrent.ExecutionContextExecutor

class SQLWorker(implicit val ctx: MyContext, implicit val ec: ExecutionContextExecutor)
    extends QueryEnrichment with QueryBlackListFriend with QueryBlackListGroup {}
