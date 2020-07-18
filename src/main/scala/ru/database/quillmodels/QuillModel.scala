package ru.database.quillmodels

import java.time.Instant
import java.util.Date

import io.getquill.{Literal, MirrorSqlDialect, SqlMirrorContext}

class MyContextT extends SqlMirrorContext(MirrorSqlDialect, Literal)

case class Woman(idwoman: Int,
                 //    login: String,
                 //  pass: String,
                 access_token: String,
                 status: String,
                 //      name: String,
                 //    comment: String,
)

case class Proxyserver(idproxyserver: String,
                       ip: String,
                       port: Int,
                       login: String,
                       pass: String,
                       status: String,
                       //     comment: String,
)

sealed trait MyTag

case class Task(idtask: String,
                statustask: String,
                // time_start: Duration.Infinite,
                delay_min: Int,
                delay_max: Int,
                blacklist: String,
                type_task: String,
                type_of_mailing: String,
                dataoff: Instant,
                timeaddtask: Instant,
                timeupdate: Instant,
                //   comment: String,
)
case class TrigerDump(id: String, triger_data: Instant)
case class WomanProxy(idwoman: Int, idproxyserver: String)
case class TaskWoman(idtask: String,
                     idwoman: Int,
                     status: String,
                     current_day: Int,
                     datatime_start: Instant,
                     datatime_latest_addition: Instant,
                     datatime_stop: Instant)

case class Searchoption(idtask: String,
                        sort: Int,
                        online: Int,
                        has_photo: Int,
                        sex: Int,
                        status: Int,
                        country: Int,
                        city: Int,
                        age_from: Int,
                        age_to: Int,
                        max_add_friend_man: Int,
                        min_add_friend_man: Int,
)

case class BlackListFriend(namelist: String, idman: Int, idwoman: Int, datatime: Instant, reason: String)

case class BlackListGroup(idgroup: String, idman: Int, idwoman: Int, datatime: Instant, reason: String)
case class ConditionSave(idtask: String, current_day: Int, datatime_start_task: Instant, datatime_latest_addition: Instant)
case class FriendList(idtask: String, id_man: Int, status: String, id_woman: Int, data_time_add: Instant)
case class DayList(idtask: String, data_day: Instant, counter: Int, start_time: Instant, stop_time: Instant, comment: String)

trait MySchemaT {

  val c: MyContextT

  import c._

  implicit val instantEncoder = MappedEncoding[Instant, Date] { i =>
    new Date(i.toEpochMilli)
  }

  implicit val instantDecoder = MappedEncoding[Date, Instant] { d =>
    Instant.ofEpochMilli(d.getTime)
  }

  private val woman = quote {
    querySchema[Woman]("woman")
  }

  private val task = quote {
    querySchema[Task]("task")
  }

  private val proxyserver = quote {
    querySchema[Proxyserver]("proxyserver")
  }

  private val taskwoman = quote {
    querySchema[TaskWoman]("taskwoman")
  }

  private val womanproxy = quote {
    querySchema[WomanProxy]("womanproxy")
  }

  private val searchoption_for_add_friend = quote {
    querySchema[Searchoption]("searchoption")
  }
  private val blacklistfriend = quote {
    querySchema[BlackListFriend]("blacklistfriend")
  }
  private val blacklistforgroup = quote {
    querySchema[BlackListGroup]("blacklistgroup")
  }
  private val condition_save = quote {
    querySchema[ConditionSave]("condition")
  }
  private val triger_dump = quote {
    querySchema[TrigerDump]("triger_dump")
  }
  private val friend_list = quote {
    querySchema[FriendList]("friend_list")
  }
  private val day_list = quote {
    querySchema[DayList]("day_list")
  }
}

//case class MyDao(c: MyContext) extends MySchema {
//  import c._
//
//  def allTask =
//    c.run(task)
//}
