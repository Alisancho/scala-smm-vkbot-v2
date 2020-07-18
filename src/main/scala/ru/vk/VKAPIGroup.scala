package ru.vk

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import com.typesafe.config.ConfigFactory

trait VKAPIGroup {
  val config         = ConfigFactory.load()
  val VK_API_VERSION = config.getString("VK_API_VERSION")

  /**
    * Метод для приглошения в группу (без капчей)
    *
    * @param ID_MAN
    * @return
    */
  def inviteToGroup(ID_MAN: Int, access_token: String, groupID: String) =
    Uri("https://api.vk.com/method/groups.invite")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "group_id"     -> groupID,
          "user_id"      -> ID_MAN.toString,
        )
      )

  /**
    * Метод для приглошения в группу (с капчей)
    *
    * @param ID_MAN
    * @param captcha_key
    * @param captcha_sid
    * @return
    */
  def inviteToGroup(ID_MAN: Int, access_token: String, groupID: String, captcha_key: String, captcha_sid: String) =
    Uri("https://api.vk.com/method/groups.invite")
      .withQuery(
        Query(
          "v"            -> VK_API_VERSION,
          "access_token" -> access_token,
          "group_id"     -> groupID,
          "user_id"      -> ID_MAN.toString,
          "captcha_key"  -> captcha_key,
          "captcha_sid"  -> captcha_sid,
        )
      )
}
