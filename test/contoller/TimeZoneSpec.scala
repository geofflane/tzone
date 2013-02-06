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

    object TestTimeZone extends Controller with TimeZoneController {
      val accountRepository = new AccountRepository {
        def findByKey(key: String) = None

        def verify(key: String) = false
      }
    }

    "convertTime sends unauthorized on a request that doesn't find an account" in {
      val result = TestTimeZone.convertBetween("America/New_York", "America/Los_Angeles", "2013-02-02T12:07:00.000")(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(UNAUTHORIZED)
    }

    "convertTime sends unauthorized on a request that doesn't pass token" in {
      val result = TestTimeZone.convertBetween("America/New_York", "America/Los_Angeles", "2013-02-02T12:07:00.000")(FakeRequest())
      status(result) must equalTo(UNAUTHORIZED)
    }

    "currentTime returns unauthorized on a request that doesn't find an account" in {
      val result = TestTimeZone.currentTime("America/Los_Angeles")(FakeRequest(GET , "/foo?token=xxx"))
      status(result) must equalTo(UNAUTHORIZED)
    }

    "currentTime returns unauthorized on a request that doesn't pass token" in {
      val result = TestTimeZone.currentTime("America/Los_Angeles")(FakeRequest(GET , "/foo?token=xxx"))
      status(result) must equalTo(UNAUTHORIZED)
    }
  }


  "TimeZone Request with auth token" should {

    object TestTimeZone extends Controller with TimeZoneController {
      val accountRepository = new AccountRepository {
        def findByKey(key: String) = Option(Account("test", key, isActive = true))

        def verify(key: String) = true
      }
    }

    "convertTime sends bad_request on a request without from timezone" in {
      val result = TestTimeZone.convertBetween(null, "America/New_York", null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
    }

    "convertTime sends bad_request on a request without to timezone" in {
      val result = TestTimeZone.convertBetween("America/New_York", null, null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
    }

    "convertTime sends bad_request on a request without a time" in {
      val result = TestTimeZone.convertBetween("America/New_York", "America/Los_Angeles", null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
    }

    "convertTime converts between two timezones" in {
      val result = TestTimeZone.convertBetween("America/New_York", "America/Los_Angeles", "2013-02-02T12:07:00.000")(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/plain")
      contentAsString(result) must contain("2013-02-02T09:07")
    }

    "currentTime returns bad_request on a request without a to timezone" in {
      val result = TestTimeZone.currentTime(null)(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(BAD_REQUEST)
    }

    "currentTime converts to another timezone" in {
      val result = TestTimeZone.currentTime("America/Los_Angeles")(FakeRequest(GET, "/foo?token=xxx"))
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/plain")
      contentAsString(result) must contain(new DateTime().getYear.toString)
    }
  }


}
