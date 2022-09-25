package org.timebank.core

sealed trait Day
object Day {
  case object Monday extends Day
  case object Tuesday extends Day
  case object Wednesday extends Day
  case object Thursday extends Day
  case object Friday extends Day
  case object Saturday extends Day
  case object Sunday extends Day
}

case class Time(hour: Int, minutes: Int)
case class TimeSlot(day: Day, start: Time, end: Time)
case class Availability(slots: List[TimeSlot])

case class ActivityId(id: UUID)
case class Activity(id: ActivityId,
                    userId: UserId,
                    description: String)