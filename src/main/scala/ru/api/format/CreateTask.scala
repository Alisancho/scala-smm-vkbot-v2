package ru.api.format

case class CreateTask(woman: Int, typeTask: String)

/**
  * actorName PROCESSING SQL
  * @param actorName
  * @param task
  */
case class ActorSystemTask(actorName: String, task: String)

object MyAPI {
  import play.api.libs.json.{Json, OFormat}

  implicit lazy val inCreateTask: OFormat[CreateTask] =
    Json.format[CreateTask]

  implicit lazy val inActorSystemTask: OFormat[ActorSystemTask] =
    Json.format[ActorSystemTask]

}
