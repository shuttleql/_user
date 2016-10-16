package com.shuttleql.services.user.utils

object TypeUtil {
  def toInt(s: Option[String]): Option[Int] = {
    s match {
      case Some(str) => {
        try {
          Some(str.toInt)
        } catch {
          case e: Exception => None
        }
      }
      case _ => None
    }
  }
}
