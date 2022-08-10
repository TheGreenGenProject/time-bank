package org.timebank.core

case class TimeBankId(value: UUID)
case class TimeBank(
  id: TimeBankId,
  admin: List[UserId],
  users: Map[UserId, User],
  creation: UTCTimestamp,
  createdBy: UserId)

case class Index[K, V](id: TimeBankId, index: Map[K, V])

case class TimeBankLink(id1: TimeBankId, id2: TimeBankId)
