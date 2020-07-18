package ru.database.helper
import java.text.SimpleDateFormat
import java.util.Date

import ru.statusmodel.TypeOfMailing

trait EnrichmentHelper {
  val dateFormatDataDay = new SimpleDateFormat("dd")
  val dateFormatTime    = new SimpleDateFormat("HH:mm:ss")
  val dateFormatData    = new SimpleDateFormat("yyyy-MM-dd")

  def output(mydata: Date) =
    Integer.valueOf(dateFormatDataDay.format(mydata))

  def parser(dey: Int): TypeOfMailing.Value = dey % 2 match {
    case 0 ⇒ TypeOfMailing.EVEN_DAYS
    case _ ⇒ TypeOfMailing.ODD_DAYS
  }
}
