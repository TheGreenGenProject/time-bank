package org.timebank.core

case class UUID(value: String)
object UUID {
  def from(str: String): Option[UUID] = ???
}
