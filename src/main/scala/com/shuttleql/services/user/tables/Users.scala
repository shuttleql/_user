package com.shuttleql.services.user.tables

import slick.driver.PostgresDriver.api._

case class User(
  id: Option[Int] = None,
  firstName: String,
  lastName: String,
  isAdmin: Boolean = false,
  isActive: Boolean = true,
  gender: String,
  email: String,
  password: String,
  level: Int = 1,
  preference: String = "Doubles"
)

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def firstName = column[String]("firstName")
  def lastName = column[String]("lastName")
  def isAdmin = column[Boolean]("isAdmin")
  def isActive = column[Boolean]("isActive")
  def gender = column[String]("gender")
  def email = column[String]("email")
  def password = column[String]("password")
  def level = column[Int]("level")
  def preference = column[String]("preference")
  def * = (id.?, firstName, lastName, isAdmin, isActive, gender, email, password, level, preference) <> (User.tupled, User.unapply)
}

