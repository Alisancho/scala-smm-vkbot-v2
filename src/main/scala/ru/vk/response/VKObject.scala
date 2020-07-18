package ru.vk.response

case class JSONVKError(error: Error)
case class Error(error_code: Int,
                 error_msg: String,
                 captcha_sid: Option[String],
                 captcha_img: Option[String],
                 error_text: Option[String],
                 request_params: Option[Seq[JSONVKRequestParams]])

case class JSONVKFriendsGet(response: JSONVKFriendsGetResponse)
case class JSONVKFriendsGetResponse(count: Int, items: scala.collection.immutable.Seq[Int])
case class JSONVKRequestParams(key: String, value: String)

case class JSONVKUsersSearch(response: JSONVKUsersSearchResponse)
case class JSONVKUsersSearchResponse(count: Int, items: scala.collection.immutable.Seq[JSONVKUsersSearchResponseItems])
case class JSONVKUsersSearchResponseItems(id: Int, first_name: String, last_name: String, track_code: String)

object VKObject {
  import play.api.libs.json.{Json, OFormat}

  implicit lazy val errorVK: OFormat[JSONVKError] =
    Json.format[JSONVKError]
  implicit lazy val error: OFormat[Error] =
    Json.format[Error]
  implicit lazy val errorRequestParams: OFormat[JSONVKRequestParams] =
    Json.format[JSONVKRequestParams]

  implicit lazy val friendsGet: OFormat[JSONVKFriendsGet] =
    Json.format[JSONVKFriendsGet]
  implicit lazy val friendsGetResponse: OFormat[JSONVKFriendsGetResponse] =
    Json.format[JSONVKFriendsGetResponse]

  implicit lazy val userSearch: OFormat[JSONVKUsersSearch] =
    Json.format[JSONVKUsersSearch]
  implicit lazy val userSearchResponse: OFormat[JSONVKUsersSearchResponse] =
    Json.format[JSONVKUsersSearchResponse]
  implicit lazy val userSearchItems: OFormat[JSONVKUsersSearchResponseItems] =
    Json.format[JSONVKUsersSearchResponseItems]
}
//Json.parse(resourceStream).as[JSONVKError]
