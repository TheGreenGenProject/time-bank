package org.timebank.core

case class UserId(value: UUID)
case class User(
  id: UserId,
  displayName: String,
  description: Option[String],
  validated: UTCTimestamp
)
case class Admin(userId: UserId)

