package org.timebank.core

case class UTCTimestamp(value: Long)
object UTCTimestamp {
  def from(ts: Long): UTCTimestamp = UTCTimestamp(ts)
}

trait Clock {
  def now(): UTCTimestamp
}

object Clock {
  def plusDays(tmstp: UTCTimestamp, days: Int) =
    UTCTimestamp(tmstp.value + (days * 24L * 3600L * 1000L))
}

object UTCClock extends Clock {
  def now() = UTCTimestamp(System.currentTimeMillis())
}