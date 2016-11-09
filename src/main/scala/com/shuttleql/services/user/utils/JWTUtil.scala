package com.shuttleql.services.user.utils

import com.typesafe.config._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtHeader, JwtClaim, JwtOptions}
import scala.util.{Try, Success, Failure}

object JWTUtil {
  val conf = ConfigFactory.load();

  def encodeUserId(id: Int): String = {
    val secret = conf.getString("jwt_secret")
    Jwt.encode(id.toString(), secret, JwtAlgorithm.HS256)
  }

  def decodeUserId(code: String): Option[Int] = {
    val secret = conf.getString("jwt_secret")
    Jwt.decode(code, secret, Seq(JwtAlgorithm.HS256)) match {
      case Success(id) =>
        TypeUtil.toInt(Some(id))
      case Failure(e) =>
        None
    }
  }
}