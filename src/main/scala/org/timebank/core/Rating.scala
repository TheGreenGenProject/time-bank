package org.timebank.core

sealed trait Rating

object Rating {

  case object OneStar extends Rating
  case object TwoStar extends Rating
  case object ThreeStar extends Rating
  case object FourStar extends Rating
  case object FiveStar extends Rating

  def from(n: Int): Option[Rating] = Option(n).collect {
    case 1 => OneStar
    case 2 => TwoStar
    case 3 => ThreeStar
    case 4 => FourStar
    case 5 => FiveStar
  }
}