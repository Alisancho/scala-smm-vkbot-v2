package ru.vk

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

trait VKAPI extends VKConfig {

  /**
    * Метод делает аккаунт онлайн
    *
    * @return
    */
  def toOnline(access_token: String) =
    Uri("https://api.vk.com/method/account.setOnline")
      .withQuery(
        Query("v" -> VK_API_VERSION, "access_token" -> access_token.toString)
      )

  /**
    * Метод формирует запрос на получения списка друзей
    * Метод используется для того чтобы узнать сколько друзей у user
    *
    * @return
    */
  def getFriends(ID_USER: Int, access_token: String) =
    Uri("https://api.vk.com/method/friends.get")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "user_id"      -> ID_USER.toString,
          "count"        -> "5000",
          "offset"       -> "0",
        )
      )
}
