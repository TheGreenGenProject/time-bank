package org.timebank.service.timebank

import org.timebank.core.{Admin, TimeBank, TimeBankId, UTCTimestamp, UUID}

case class LinkRequestId(id: UUID)
case class TimeBankLinkRequest(id: LinkRequestId,
                               admin: Admin,
                               requester: TimeBankId,
                               requested: TimeBankId,
                               timestamp: UTCTimestamp)

trait TimeBankService[M[_]] {

  def byId(id: TimeBankId): M[Option[TimeBank]]

  def enable(admin: Admin, id: TimeBankId): M[Unit]

  def disable(admin: Admin, id: TimeBankId): M[Unit]

  def userCount(id: TimeBankId): M[Int]

  def areLinked(id1: TimeBankId, id2: TimeBankId): M[Boolean]

  // Linking 2 time-banks to allow transactions between users from these time banks

  def requestLink(admin: Admin,
                  requester: TimeBankId,
                  requested: TimeBankId): M[Unit]

  def acceptLinkRequest(admin: Admin, id: LinkRequestId): M[Unit]

  def rejectLinkRequest(admin: Admin, id: LinkRequestId, reason: String): M[Unit]

}
