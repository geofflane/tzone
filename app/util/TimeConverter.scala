package util

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import scala.collection.JavaConverters._

/**
 *
 * @author geoff
 * @since 2/6/2013
 */
object TimeConverter {
  val formatter = ISODateTimeFormat.dateTime() //.dateHourMinuteSecondMillis()

  def format(time: DateTime) = formatter.print(time)

  def timeZones: List[String] = DateTimeZone.getAvailableIDs.asScala.toList.sorted

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
        case e: IllegalArgumentException => {
          Logger.warn("Invalid date time string %s".format(timeString))
          None
        }
      }
    }
  }
}
