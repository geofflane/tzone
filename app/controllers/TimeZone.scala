package controllers

import play.api.mvc._
import org.joda.time.{DateTimeZone, DateTime}
import collection.JavaConversions._
import org.joda.time.format.ISODateTimeFormat
import data.{DbAccountRepository, AccountRepository}
import scala.Some

/**
 * 
 * @author geoff
 * @since 2/2/2013
 */
object TimeZone extends Controller with TimeZoneController {
  val accountRepository = DbAccountRepository
}

trait TimeZoneController {
  this: Controller =>

  val accountRepository: AccountRepository

  val formatter = ISODateTimeFormat.dateHourMinuteSecondMillis()
  val knownTimeZones: Set[String] = DateTimeZone.getAvailableIDs.toSet

  def convertBetween(from: String, to:String, timeString:String) = Action { implicit request =>
    withAuthorization {
      (timeZoneFor(from),  timeZoneFor(to)) match {
        case (Some(f), Some(t)) => {
          parseTime(f, timeString) match {
            case (Some(time)) => Ok(formatter.print(time.toDateTime(t)))
            case None => BadRequest("Must send a valid time")
          }
        }
        case (_, _) => BadRequest("Unknown from (%s) or to (%s) timezones".format(from, to))
      }
    }
  }

  def currentTime(to:String) = Action { implicit request =>
    withAuthorization {
      timeZoneFor(to) match {
        case Some(t) => Ok(formatter.print(new DateTime().toDateTime(t)))
        case None => BadRequest("Unknown to timezone (%s)".format(to))
      }
    }
  }

  private def timeZoneFor(id: String): Option[DateTimeZone] = id match {
    case null => None
    case _ => {
      try {
        Option(DateTimeZone.forID(id))
      } catch {
        case e: IllegalArgumentException => None
      }
    }
  }

  private def parseTime(from: DateTimeZone, timeString: String): Option[DateTime] = timeString match {
    case null => None
    case _ => {
      try {
        Option(formatter.withZone(from).parseDateTime(timeString))
      } catch {
        case e: IllegalArgumentException => None
      }
    }
  }

  private def withAuthorization[A](fn: => Result)(implicit request: Request[A]): Result = {
    request.getQueryString("token") match {
      case Some(token) => {
        if (! accountRepository.verify(token)) {
          Unauthorized("Unknown token")
        } else {
          fn
        }
      }
      case None => Unauthorized("Unknown token")
    }
  }
}
