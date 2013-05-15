package data

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.Logger

case class Account (name: String, key: String, isActive: Boolean)

trait AccountRepository {
  def verify(key:String): Boolean
  def findByKey(key: String): Option[Account]
}

object DbAccountRepository extends AccountRepository {

  val account = {
    str("name") ~
    str("key") ~
    bool("isActive") map {
        case n~k~isActive => Account(n,k,isActive)
      }
  }

  def save(name:String, key:String) = {
    DB.withConnection { implicit conn =>
      SQL("INSERT INTO Account (accountName, accountKey, isActive) VALUES({name}, {key}, {isActive});")
        .on("name" -> name, "key" -> key, "isActive" -> true).executeInsert()
    }
    Account(name, key, isActive = true)
  }

  def delete(key:String) = {
    DB.withConnection { implicit conn =>
      SQL("DELETE FROM Account WHERE key={key};")
        .on("key" -> key).executeUpdate()
    }
  }

  def activate = modifyActive(isActive = true) _

  def deactivate = modifyActive(isActive = false) _

  private def modifyActive(isActive: Boolean)(key: String) = {
    DB.withConnection { implicit conn =>
      Logger.debug("Modify active")
      SQL("UPDATE Account SET isActive={isActive} WHERE accountKey={key};")
        .on("isActive" -> isActive, "key" -> key).executeUpdate()
    }
  }

  def verify(key:String) = {
    findByKey(key) map { _.isActive } getOrElse(false)
  }

  def findByKey(key: String): Option[Account] = {
    DB.withConnection { implicit conn =>
      val selectAccounts = SQL("SELECT a.accountName as name, a.accountKey as key, a.isActive FROM Account a WHERE a.accountKey={key};")
      selectAccounts.on("key" -> key).as(account singleOpt)
    }
  }
}