package ru.vk

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

trait VKAPIFriend extends VKConfig {

  /**
    * Метод отправляет приглошение в друзья (без капчи)
    *
    * @param ID_MAN ID аккаунта кого нужно пригласить
    * @return готовый URL
    */
  def addFriend(ID_MAN: Int, access_token: String) =
    Uri("https://api.vk.com/method/friends.add")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "user_id"      -> ID_MAN.toString
        )
      )

  /**
    * Метод отправляет приглошение в друзья (с капчей)
    *
    * @param ID_MAN ID аккаунта кого нужно пригласить
    * @param captcha_key
    * @param captcha_sid
    * @return готовый URL
    */
  def addFriend(ID_MAN: Int, access_token: String, captcha_key: String, captcha_sid: String) =
    Uri("https://api.vk.com/method/friends.add")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "user_id"      -> ID_MAN.toString,
          "count"        -> "5000",
          "offset"       -> "0",
          "captcha_key"  -> captcha_key,
          "captcha_sid"  -> captcha_sid,
        )
      )

  /**
    * Метод удаляет чувака со страницы (без капчи)
    *
    * @param ID_MAN
    * @param access_token
    * @return
    */
  def deleteFriend(ID_MAN: Int, access_token: String) =
    Uri("https://api.vk.com/method/friends.delete")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "user_id"      -> ID_MAN.toString,
        )
      )

  /**
    * Метод удаляет чувака со страницы (с капчей)
    *
    * @param ID_MAN
    * @param access_token
    * @return
    */
  def deleteFriend(ID_MAN: Int, access_token: String, captcha_key: String, captcha_sid: String) =
    Uri("https://api.vk.com/method/friends.delete")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "user_id"      -> ID_MAN.toString,
          "captcha_key"  -> captcha_key,
          "captcha_sid"  -> captcha_sid,
        )
      )

}
