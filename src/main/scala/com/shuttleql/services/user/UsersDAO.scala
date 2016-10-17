package com.shuttleql.services.user

import com.roundeights.hasher.Hasher
import com.shuttleql.services.user.tables.{User, Users}
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object UsersDAO extends TableQuery(new Users(_)) {
  def initDb() = {
    Database.forConfig("db")
  }

  def setupTables(): Option[Unit] = {
    val db = initDb

    try {
      Option(Await.result(db.run(this.schema.create), Duration.Inf))
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def getAll(): Option[Seq[User]] = {
    val db = initDb

    try {
      val results: Seq[User] = Await.result(db.run(this.result), Duration.Inf)
      Option(results)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def getOne(id: Int): Option[User] = {
    val db = initDb

    try {
      Await.result(db.run(this.filter(_.id === id).result).map(_.headOption), Duration.Inf)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def update(id: Int, user: User): Option[User] = {
    val db = initDb

    try {
      val updatedUser = user.copy(id = Some(id))

      Await.ready(db.run(this.filter(_.id === id).update(updatedUser)), Duration.Inf)
      Some(updatedUser)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def deactivate(id: Int): Option[_] = {
    val db = initDb

    try {
      val isActive = for { u <- this if u.id === id } yield u.isActive
      val updateAction = isActive.update(false)

      Some(Await.ready(db.run(updateAction), Duration.Inf))
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }

  }

  def create(user: User): Option[User] = {
    val db = initDb

    val hashedPw = Hasher(user.password).bcrypt
    val newUser = user.copy(password = hashedPw)

    try {
      val result: User = Await.result(db.run(this returning this += newUser), Duration.Inf)
      Option(result)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }
}
