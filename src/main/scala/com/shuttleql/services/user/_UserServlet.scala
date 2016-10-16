package com.shuttleql.services.user

import com.shuttleql.services.user.models.User
import com.shuttleql.services.user.utils.TypeUtil
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._

class _UserServlet extends UserServiceStack with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
  }

  get("/users") {
    val all = List(
      User(1, "Clement", "Hoang", true, true, "male", "clem@mail.com", "123", 4, "singles" ),
      User(2, "Jason", "Fang", false, true, "male", "jason@mail.com", "123", 3, "singles" )
    )

    Ok(all)
  }

  get("/users/:id") {
    TypeUtil.toInt(params.get("id")) match {
      case Some(i: Int)=>
        Ok(User(1, "Clement", "Hoang", true, true, "male", "clem@mail.com", "123", 4, "singles" ))
      case _ =>
        BadRequest(reason = "Invalid 'id' parameter.")
    }
  }

  post("/users") {
    val newUser = parsedBody.extract[User]

    Created(newUser)
  }

  put("/users/:id") {
    val updatedUser = parsedBody.extract[User]

    Ok(updatedUser)
  }

  delete("/users/:id") {
    NoContent(reason = "Successfully deactivated user")
  }

  after() {
  }

}
