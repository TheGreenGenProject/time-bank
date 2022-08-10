package org.timebank.core

case class Email(value: String)
object Email {
  val EmailRE = "^([a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-\\.]+)$".r
  def from(str: String): Option[Email] = str match {
    case EmailRE(email) => Some(Email(email))
    case _ => None
  }
}

case class ActivationCode(value: String)

sealed trait ValidationMethod
object ValidationMethod {
  case class EmailValidation(email: Email, code: ActivationCode, sent: UTCTimestamp) extends ValidationMethod
  case class AdminValidation(admin: UserId) extends ValidationMethod
}

case class Validation(method: ValidationMethod, timestamp: UTCTimestamp)