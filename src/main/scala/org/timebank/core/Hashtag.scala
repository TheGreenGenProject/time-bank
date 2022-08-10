package org.timebank.core

case class Hashtag(value: String)

object Hashtag {

  val HashtagRE = "^(#[a-zA-Z0-9]{3,})$".r

  def from(str: String): Option[Hashtag] = str.trim match {
    case HashtagRE(ht) => Some(Hashtag(ht))
    case _ => None
  }

}