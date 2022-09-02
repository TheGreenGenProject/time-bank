package org.timebank.core

case class UTCTimestamp(value: Long)
object UTCTimestamp {
  def from(ts: Long): UTCTimestamp = UTCTimestamp(ts)
}

trait Clock {
  def now(): UTCTimestamp
}
object UTCClock extends Clock {
  def now() = UTCTimestamp(System.currentTimeMillis())
}