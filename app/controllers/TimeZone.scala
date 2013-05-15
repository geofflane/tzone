package controllers

import play.api.mvc._
import play.api.libs.json.Json
import org.joda.time.DateTime
import data.{DbAccountRepository, AccountRepository}
import scala.Some
import util.TimeConverter._

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

  val Text = Accepting("text/plain")
  val accountRepository: AccountRepository

  def convertBetween(from: String, to:String, timeString:String) = Action { implicit request =>
    withAuthorization( {
      (timeZoneFor(from),  timeZoneFor(to)) match {
        case (Some(f), Some(t)) => {
          parseTime(f, timeString) match {
            case (Some(time)) => renderTime(new DateTime(time).toDateTime(t))
            case None => BadRequest("Must send a valid time")
          }
        }
        case (_, _) => BadRequest("Unknown from (%s) or to (%s) timezones".format(from, to))
      }
    } )
  }

  def currentTime(to:String) = Action { implicit request =>
    withAuthorization( {
      timeZoneFor(to) match {
        case Some(t) => renderTime(new DateTime(t))
        case None => BadRequest("Unknown to timezone (%s)".format(to))
      }
    } )
  }

  private def renderTime[A](time: DateTime)(implicit request: Request[A]): Result = {
    render {
      case Accepts.Json => Ok(Json.obj("result" -> format(time)))
      case _ => Ok(format(time))
    }
  }

  private def withAuthorization[A](body: => Result)(implicit request: Request[A]): Result = {
    request.getQueryString("token") match {
      case Some(token) => {
        if (! accountRepository.verify(token)) {
          Unauthorized("Unknown token")
        } else {
          body
        }
      }
      case None => Unauthorized("Unknown token")
    }
  }
}
