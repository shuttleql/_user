package com.shuttleql.services.user.models

case class User(
  id: Long,
  firstName: String,
  lastName: String,
  isAdmin: Boolean,
  isActive: Boolean,
  gender: String,
  email: String,
  password: String,
  level: Int,
  preference: String
)
