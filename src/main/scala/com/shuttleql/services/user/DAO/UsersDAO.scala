package com.shuttleql.services.user.DAO

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import com.roundeights.hasher.Hasher
import com.shuttleql.services.user.tables.{User, Users}
import com.typesafe.config.ConfigFactory
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object UsersDAO extends TableQuery(new Users(_)) {
  val conf = ConfigFactory.load()

  val creds = new BasicAWSCredentials(conf.getString("amazon.access_key"), conf.getString("amazon.secret_key"))
  val snsClient = new AmazonSNSClient(creds)
  snsClient.setRegion(Region.getRegion(Regions.US_WEST_2))

  def broadcastUserUpdate(): Unit = {
    val publishReq = new PublishRequest()
      .withTopicArn(conf.getString("amazon.topic_arn"))
      .withSubject("update")
      .withMessage("{ \"resource\": \"users\" }")

    snsClient.publish(publishReq)
  }

  def initDb = {
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

  def getAll: Option[Seq[User]] = {
    val db = initDb

    try {
      val results: Seq[User] = Await.result(db.run(this.filter(_.isActive).result), Duration.Inf)
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
      Await.result(db.run(this.filter(_.id === id).filter(_.isActive).result).map(_.headOption), Duration.Inf)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def getByEmailPass(email: String, password: String): Option[User] = {
    val db = initDb

    try {
      val hashedPw = Hasher(password).bcrypt
      val user = Await.result(db.run(this.filter(_.email === email).filter(_.isActive).result), Duration.Inf).headOption
      user match {
        case Some(u: User) => {
          (hashedPw hash= u.password) match {
            case true => Some(u)
            case false => None
          }
        }
        case None => None
      }
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
      broadcastUserUpdate
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
      broadcastUserUpdate
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
      broadcastUserUpdate
      db.close
    }
  }
}
