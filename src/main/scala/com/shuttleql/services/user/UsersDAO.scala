package com.shuttleql.services.user

import com.shuttleql.services.user.tables.{User, Users}
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object UsersDAO extends TableQuery(new Users(_)) {
  def initDb() = {
    Database.forConfig("db")
  }

  def setupTables(): Option[Unit] = {
    val db = initDb

    val users = TableQuery[Users]

    try {
      Option(Await.result(db.run(users.schema.create), Duration.Inf))
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def getAll(): Option[Seq[User]] = {
    val db = initDb

    try {
      val users = TableQuery[Users]
      val results: Seq[User] = Await.result(db.run(users.result), Duration.Inf)
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
      val users = TableQuery[Users]
      Await.result(db.run(users.filter(_.id === id).result).map(_.headOption), Duration.Inf)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def update(id: Int, user: User): Option[User] = {
    val db = initDb

    try {
      val users = TableQuery[Users]
      val updatedUser = user.copy(id = Some(id))

      Await.ready(db.run(users.filter(_.id === id).update(updatedUser)), Duration.Inf)
      Some(updatedUser)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def deactivate(id: Int): Option[models.Success] = {
    val db = initDb

    try {
      val users = TableQuery[Users]

      val isActive = for { u <- users if u.id === id } yield u.isActive
      val updateAction = isActive.update(false)


      Await.ready(db.run(updateAction), Duration.Inf)
      Some(models.Success(message = "Successfully deactivated user."))
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }

  }

  def create(newUser: User): Option[User] = {
    val db = initDb

    try {
      val users = TableQuery[Users]
      val result: User = Await.result(db.run(users returning users += newUser), Duration.Inf)
      Option(result)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }
}
