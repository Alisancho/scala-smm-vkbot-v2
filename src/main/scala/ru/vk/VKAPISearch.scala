package ru.vk

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import ru.core.actors.ActorProcessingSupervisor.MainTask

trait VKAPISearch extends VKConfig {
  val mainTask: MainTask

  def getLisfBoy(access_token: String) =
    Uri("https://api.vk.com/method/users.search")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "sort"         -> mainTask.searchlist.get.sort.toString,
          "online"       -> mainTask.searchlist.get.online.toString,
          "has_photo"    -> mainTask.searchlist.get.has_photo.toString,
          "sex"          -> mainTask.searchlist.get.sex.toString,
          "status"       -> mainTask.searchlist.get.status.toString,
          "country"      -> mainTask.searchlist.get.country.toString,
          "city"         -> mainTask.searchlist.get.city.toString,
          "age_from"     -> mainTask.searchlist.get.age_from.toString,
          "age_to"       -> mainTask.searchlist.get.age_to.toString,
          "offset"       -> "0",
          "count"        -> "1000",
        )
      )

  def getLisfBoy(access_token: String, age: Int) =
    Uri("https://api.vk.com/method/users.search")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "sort"         -> mainTask.searchlist.get.sort.toString,
          "online"       -> mainTask.searchlist.get.online.toString,
          "has_photo"    -> mainTask.searchlist.get.has_photo.toString,
          "sex"          -> mainTask.searchlist.get.sex.toString,
          "status"       -> mainTask.searchlist.get.status.toString,
          "country"      -> mainTask.searchlist.get.country.toString,
          "city"         -> mainTask.searchlist.get.city.toString,
          "age_from"     -> age.toString,
          "age_to"       -> age.toString,
          "offset"       -> "0",
          "count"        -> "1000",
        )
      )
}
