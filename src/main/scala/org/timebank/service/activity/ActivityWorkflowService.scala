package org.timebank.service.activity

import org.timebank.core.{ActivityId, TimeSlot, UTCTimestamp, UUID, UserId}

case class ActivityRequestId(id: UUID)
case class ActivityRequest(id: ActivityRequestId,
                           requester: UserId,
                           activity: ActivityId,
                           timestamp: UTCTimestamp,
                           validUntil: UTCTimestamp,
                           timeslots: List[TimeSlot],
                           message: String)


trait ActivityWorkflowService[M[_]] {

  def newRequest(requester: UserId,
                 activity: ActivityId,
                 timeSlots: List[TimeSlot],
                 message: Option[String]): M[Either[String, ActivityRequestId]]

  def rejectRequest(requestId: ActivityRequestId,
                    organizer: UserId,
                    reason: Option[String]): M[Either[String, Unit]]

  def acceptRequest(requestId: ActivityRequestId,
                    organizer: UserId,
                    reason: Option[String]): M[Either[String, Unit]]

  def cancelRequest(requestId: ActivityRequestId,
                    reason: Option[String]): M[Either[String, Unit]]

  def ready(requestId: ActivityRequestId, organizer: UserId): M[Either[String, Unit]]

  def organizerValidation(requestId: ActivityRequestId, userId: UserId): M[Either[String, Unit]]

  def requesterValidation(requestId: ActivityRequestId, userId: UserId): M[Either[String, Unit]]

}
