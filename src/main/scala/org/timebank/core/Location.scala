package org.timebank.core

case class Url(value: String)

sealed trait Location
object Location {
  case class Online(url: Url) extends Location
  case class MapLink(url: Url) extends Location
  case class PhysicalAddress(address: String) extends Location
  case class PostCode(code: String) extends Location
}

