package util

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat

/**
 *
 * @author geoff
 * @since 2/6/2013
 */
object TimeConverter {
  val formatter = ISODateTimeFormat.dateHourMinuteSecondMillis()

  def format(time: DateTime) = formatter.print(time)

  def timeZoneFor(id: String): Option[DateTimeZone] = id match {
    case null => None
    case _ => {
      try {
        Option(DateTimeZone.forID(id))
      } catch {
        case e: IllegalArgumentException => None
      }
    }
  }

  def parseTime(from: DateTimeZone, timeString: String): Option[DateTime] = timeString match {
    case null => None
    case _ => {
      try {
        Option(formatter.withZone(from).parseDateTime(timeString))
      } catch {
        case e: IllegalArgumentException => None
      }
    }
  }
}
