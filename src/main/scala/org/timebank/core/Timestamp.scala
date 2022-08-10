package org.timebank.core

case class UTCTimestamp(value: Long)
object UTCTimestamp {
  def from(ts: Long): UTCTimestamp = UTCTimestamp(ts)
}
