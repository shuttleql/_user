package com.shuttleql.services.user

import com.shuttleql.services.user.tables.{User, Users}
import com.shuttleql.services.user.utils.TypeUtil
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._

class _UserServlet extends UserServiceStack with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
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
            Ok(reason = "Success.")
          case None =>
            NotFound(reason = "Something went wrong.")
        }
      case _ =>
        BadRequest(reason = "Invalid 'id' parameter.")
    }
  }

  after() {
  }

}
