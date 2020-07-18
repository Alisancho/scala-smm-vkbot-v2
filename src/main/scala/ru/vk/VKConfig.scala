package ru.vk

import com.typesafe.config.ConfigFactory

trait VKConfig {
  val config         = ConfigFactory.load()
  val VK_API_VERSION = config.getString("VK_API_VERSION")
}
