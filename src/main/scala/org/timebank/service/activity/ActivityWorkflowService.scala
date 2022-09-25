package org.timebank.service.activity

import org.timebank.core.{ActivityId, TimeSlot, UTCTimestamp, UUID, UserId}

case class ActivityRequestId(id: UUID)
case class ActivityRequest(id: ActivityRequestId,
                           requester: Requester,
                           activity: ActivityId,
                           timestamp: UTCTimestamp,
                           validUntil: UTCTimestamp,
                           timeslots: List[TimeSlot],
                           message: String)


trait ActivityWorkflowService[M[_]] {

  def newRequest(requester: Requester,
                 activity: ActivityId,
                 timeSlots: List[TimeSlot],
                 message: Option[String]): M[ActivityRequestId]

  def rejectRequest(requestId: ActivityRequestId,
                    organizer: Owner,
                    reason: Option[String]): M[Unit]

  def acceptRequest(requestId: ActivityRequestId,
                    organizer: Owner,
                    timeSlot: TimeSlot): M[Unit]

  def cancelRequest(requestId: ActivityRequestId,
                    userId: UserId,
                    reason: Option[String]): M[Unit]

  def ready(requestId: ActivityRequestId, organizer: Owner): M[Unit]

  def organizerValidation(requestId: ActivityRequestId, userId: Owner): M[Unit]

  def requesterValidation(requestId: ActivityRequestId, userId: Requester): M[Unit]

}
