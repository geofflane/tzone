package contoller

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.FakeRequest
import org.joda.time.DateTime
import play.api.mvc.Controller
import controllers.TimeZoneController
import data.{Account, AccountRepository}

/**
 *
 * @author geoff
 * @since 2/2/2013
 */
class TimeZoneSpec extends Specification {

  "TimeZone Request without auth token" should {

    object TestTimeZoneBadToken extends Controller with TimeZoneController {
      val accountRepository = new AccountRepository {
        def findByKey(key: String) = None
        def verify(key: String) = false
        def save(name:String, key: String) = Account(name, key, isActive = true)
        def activate(key: String) = 1
        def deactivate(key: String) = 1
        def delete(key: String) = 1
      }
    }

    "convertTime sends unauthorized on a request that doesn't find an account" in {
      val result = TestTimeZoneBadToken.convertBetween("America/New_York", "America/Los_Angeles", "2013-02-02T12:07:00.000")(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) must equalTo("Unknown token")
    }

    "convertTime sends unauthorized on a request that doesn't pass token" in {
      val result = TestTimeZoneBadToken.convertBetween("America/New_York", "America/Los_Angeles", "2013-02-02T12:07:00.000")(FakeRequest())
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) must equalTo("No token passed")
    }

    "currentTime returns unauthorized on a request that doesn't find an account" in {
      val result = TestTimeZoneBadToken.currentTime("America/Los_Angeles")(FakeRequest(GET , "/foo?token=xxx"))
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) must equalTo("Unknown token")
    }

    "currentTime returns unauthorized on a request that doesn't pass token" in {
      val result = TestTimeZoneBadToken.currentTime("America/Los_Angeles")(FakeRequest(GET , "/foo?token=xxx"))
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) must equalTo("Unknown token")
    }
  }


  "TimeZone Request with auth token" should {

    object TestTimeZoneGoodToken extends Controller with TimeZoneController {
      val accountRepository = new AccountRepository {
        def findByKey(key: String) = Option(Account("test", key, isActive = true))
        def verify(key: String) = true
        def save(name:String, key: String) = Account(name, key, isActive = true)
        def activate(key: String) = 1
        def deactivate(key: String) = 1
        def delete(key: String) = 1
      }
    }

    "convertTime sends bad_request on a request without from timezone" in {
      val result = TestTimeZoneGoodToken.convertBetween(null, "America/New_York", null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must contain("Unknown from")
    }

    "convertTime sends bad_request on a request without to timezone" in {
      val result = TestTimeZoneGoodToken.convertBetween("America/New_York", null, null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must contain("Unknown from")
    }

    "convertTime sends bad_request on a request without a time" in {
      val result = TestTimeZoneGoodToken.convertBetween("America/New_York", "America/Los_Angeles", null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must contain("Must send a valid time")
    }

    "convertTime converts between two timezones" in {
      val result = TestTimeZoneGoodToken.convertBetween("America/New_York", "America/Los_Angeles", "2013-02-02T12:07:00.000")(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(OK)
      println("xxx: " + contentType(result))
      contentType(result) must beSome.which(_ == "text/plain")
      contentAsString(result) must contain("2013-02-02T09:07")
    }

    "currentTime returns bad_request on a request without a to timezone" in {
      val result = TestTimeZoneGoodToken.currentTime(null)(FakeRequest(GET, "/convertCurrent?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must contain("Unknown to timezone")
    }

    "currentTime converts to another timezone" in {
      val result = TestTimeZoneGoodToken.currentTime("America/Los_Angeles")(FakeRequest(GET, "/convertCurrent?token=xxx"))
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/plain")
      contentAsString(result) must contain(new DateTime().getYear.toString)
    }
  }
}
