package data

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.FakeApplication

/**
 *
 * @author geoff
 * @since 2/3/2013
 */
class DbAccountRepositorySpec extends Specification {
  val key = "xxxxx"
  val inMemoryDatabase = Map[String, String](
      "db.default.driver" -> "org.h2.Driver",
     "db.default.url"    -> "jdbc:h2:mem:test;MODE=PostgreSQL"
  )

  "DbAccountRepository" should {

    "return none when there is nothing saved" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase)) {
        DbAccountRepository.findByKey(key) must beNone
      }
    }

    "save and find an item by key" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase)) {
        DbAccountRepository.save("test", key)
        DbAccountRepository.findByKey(key) must beSome
      }
    }

    "deactivate an item when deactivate is called" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase)) {
        DbAccountRepository.save("test", key)
        DbAccountRepository.deactivate(key)
        def acc = DbAccountRepository.findByKey(key)
        acc must beSome.which(a => ! a.isActive)
      }
    }
  }
}
