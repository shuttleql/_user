package com.shuttleql.services.user

import com.shuttleql.services.user.tables.{User, Users}
import com.shuttleql.services.user.utils.TypeUtil
import com.shuttleql.services.user.DAO.UsersDAO
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import com.typesafe.config._
import com.gandalf.HMACAuth

// Strong params
case class UserIdParams(ids: List[Int])

class _UserServlet extends UserServiceStack with JacksonJsonSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  val conf = ConfigFactory.load();

  private def getRequest = enrichRequest(request)
  private def getResponse = enrichResponse(response)

  before() {
    auth
    contentType = formats("json")
  }

  def auth() {
    val token = getRequest.header("Authorization")
    val key = getRequest.header("Authorization-Key")
    val secret = conf.getString("secrets.hmac_secret")

    (token, key) match {
      case (Some(t), Some(k)) =>
        val split = t.split("HMAC ")
        split.length match {
          case 2 =>
            HMACAuth.validateHost(split(1), k, secret) match {
              case true => return
              case false =>
                halt(status=401, reason="Forbidden");
            }
          case _ =>
            halt(status=401, reason="Forbidden");
        }
      case _ =>
        halt(status=401, reason="Forbidden");
    }
  }

  get("/setup") {
    UsersDAO.setupTables() match {
      case Some(results) =>
        Ok(reason = "Success.")
      case None =>
        InternalServerError(reason = "Error creating tables.")
    }
  }

  get("/users") {
    UsersDAO.getAll() match {
      case Some(results) =>
        Ok(results)
      case None =>
        NoContent(reason = "No users found.")
    }
  }

  post("/usersByIds") {
    try {
      val newUsers = parsedBody.extract[UserIdParams]
      UsersDAO.getAllByIds(newUsers.ids) match {
        case Some(results) =>
          Ok(results)
        case None =>
          NoContent(reason = "No users found.")
      }
    } catch {
      case e: Exception =>
        InternalServerError(reason = "Problem with payload.")
    }
  }

  get("/users/:id") {
    TypeUtil.toInt(params.get("id")) match {
      case Some(i: Int) =>
        UsersDAO.getOne(i) match {
          case Some(result) =>
            Ok(result)
          case None =>
            NotFound(reason = "Specified user not found.")
        }
      case _ =>
        BadRequest(reason = "Invalid 'id' parameter.")
    }
  }

  post("/users") {
    try {
      val newUser = parsedBody.extract[User]

      UsersDAO.create(newUser) match {
        case Some(results) =>
          Created(results)
        case None =>
          InternalServerError(reason = "Cannot create user.")
      }
    } catch {
      case e: Exception =>
        InternalServerError(reason = "Problem with payload.")
    }
  }

  put("/users/:id") {
    val updatedUser = parsedBody.extract[User]

    TypeUtil.toInt(params.get("id")) match {
      case Some(i: Int) =>
        UsersDAO.update(i, updatedUser) match {
          case Some(result) =>
            Ok(result)
          case None =>
            NotFound(reason = "Something went wrong.")
        }
      case _ =>
        BadRequest(reason = "Invalid 'id' parameter.")
    }
  }

  delete("/users/:id") {
    TypeUtil.toInt(params.get("id")) match {
      case Some(i: Int) =>
        UsersDAO.deactivate(i) match {
          case Some(_) =>
            NoContent(reason = "Success.")
          case None =>
            NotFound(reason = "Something went wrong.")
        }
      case _ =>
        BadRequest(reason = "Invalid 'id' parameter.")
    }
  }

  post("/users/auth") {
    val credentials = try {
      Some(parsedBody.extract[Map[String, String]])
    } catch {
      case e: Exception => None
    }

    credentials match {
      case Some(c: Map[String, String]) => {
        if (!c.contains("email") || !c.contains("password")) {
          BadRequest(reason = "Must specify email and password.")
        } else {
          val email = c.get("email").getOrElse("")
          val password = c.get("password").getOrElse("")

          UsersDAO.getByEmailPass(email, password) match {
            case Some(result: User) => 
              Ok(result)
            case None => 
              BadRequest(reason = "Incorrect credentials.")
          }
        }
      }
      case None => BadRequest(reason = "Malformed request.")
    }
  }

  after() {
  }

}
