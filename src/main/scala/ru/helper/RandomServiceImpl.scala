package ru.helper

import java.security.SecureRandom
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.UUID

import com.google.common.io.BaseEncoding

import scala.util.Random

class RandomServiceImpl {

  def createUuid: UUID = UUID.randomUUID()

  def createApplicationId(): String = {
    val formattedDate = formatInstant(
      DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"),
      timeService
    )
    val randomCharsCount = 6
    formattedDate + "IBL" + randomStream(integersChars)
      .take(randomCharsCount)
      .mkString
  }

  def actorID: String = "3-" + createHex(4)

  private def createHex(numberOfBytes: Int): String = {
    val array = new Array[Byte](numberOfBytes)
    random.nextBytes(array)
    BaseEncoding.base16().lowerCase().encode(array)
  }

  private def randomStream(alphabet: String) = {
    Stream.continually(random.nextInt(alphabet.length)).map(alphabet)
  }

  private def formatInstant(formatter: DateTimeFormatter, instant: Instant): String =
    formatter.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))

  private def timeService: Instant = Instant.now()

  private val integersChars = ('0' to '9').mkString

  private def random = new Random(new SecureRandom())
}
