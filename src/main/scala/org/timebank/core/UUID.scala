package org.timebank.core

case class UUID(value: String)

object UUID {

  def from(str: String): Option[UUID] = ???

}


trait UUIDGenerator {
  def random(): UUID
}

object DefaultUUIDGenerator extends UUIDGenerator {
  override def random(): UUID =
    UUID(java.util.UUID.randomUUID().toString)
}