package org.timebank.service.activity

import org.timebank.core.{Clock, TimeSlot, UTCTimestamp, UserId}


sealed trait ActivityWorkflowState {
  def requestId: ActivityRequestId
  def timestamp: UTCTimestamp
}
sealed trait FinalState extends ActivityWorkflowState

// States
final case class Init(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp) extends ActivityWorkflowState
final case class Requested(request: ActivityRequest) extends ActivityWorkflowState {
  override def requestId = request.id
  override def timestamp = request.timestamp
}
final case class Accepted(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  timeSlot: TimeSlot,
  message: Option[String]) extends ActivityWorkflowState
final case class Rejected(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  reason: Option[String]) extends FinalState
final case class Cancelled(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  byUser: Option[UserId],
  reason: Option[String]) extends FinalState
final case class ActivityValidation(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  requester: Option[Requester],
  organizer: Option[Owner]) extends ActivityWorkflowState {
  def isValidated = requester.nonEmpty && organizer.nonEmpty
}
final case class Done(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp) extends FinalState
final case class WorkflowError(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  error: String) extends FinalState

// Actions
sealed trait ActivityWorkflowAction {
  def requestId: ActivityRequestId
  def timestamp: UTCTimestamp
}
final case class Request(request: ActivityRequest) extends ActivityWorkflowAction {
  def requestId = request.id
  def timestamp = request.timestamp
}
final case class Accept(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  user: Owner,
  timeSlot: TimeSlot) extends ActivityWorkflowAction
final case class Reject(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  user: Owner,
  reason: Option[String]) extends ActivityWorkflowAction
final case class Cancel(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  user: UserId,
  reason: Option[String]) extends ActivityWorkflowAction
final case class ActivityReady(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  user: Owner) extends ActivityWorkflowAction
final case class RequesterValidate(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  user: Requester) extends ActivityWorkflowAction
final case class OrganizerValidate(
  requestId: ActivityRequestId,
  timestamp: UTCTimestamp,
  user: Owner) extends ActivityWorkflowAction



object ActivityWorkflowStateMachine {

  // State transition implementation
  def handle(clock: Clock)
            (state: ActivityWorkflowState)
            (action: ActivityWorkflowAction): ActivityWorkflowState = (state ---> action) match {
    // Cannot move state-machine from a final state
    case (event: FinalState) ---> _ => event
    // Workflow logic
    case (_: Init) ---> (req: Request) =>
      Requested(req.request)
    case (r: Requested) ---> (cancel: Cancel) =>
      Cancelled(r.requestId, clock.now(), byUser = Some(cancel.user), reason = cancel.reason)
    case (r: Requested) ---> (reject: Reject) =>
      Rejected(r.requestId, clock.now(), reason = reject.reason)
    case (r: Requested) ---> (accept: Accept) =>
      Accepted(r.requestId, clock.now(), accept.timeSlot, message = None)
    case (_: Accepted) ---> (cancel: Cancel) =>
      Cancelled(state.requestId, clock.now(), byUser = Some(cancel.user), reason = cancel.reason)
    case (s: Accepted) ---> (_: ActivityReady) =>
      ActivityValidation(s.requestId, clock.now(), None, None)
    case (av: ActivityValidation) ---> (validate: RequesterValidate) =>
      val s = ActivityValidation(state.requestId, clock.now(), requester = Some(validate.user), organizer = av.organizer)
      if(s.isValidated) Done(s.requestId, s.timestamp) else s
    case (av: ActivityValidation) ---> (validate: OrganizerValidate) =>
      val s = ActivityValidation(state.requestId, clock.now(), requester = av.requester, organizer = Some(validate.user))
      if(s.isValidated) Done(s.requestId, s.timestamp) else s
    // Failure cases
    case _ ---> unexpectedAction =>
      WorkflowError(state.requestId, clock.now(), s"Unexpected action $unexpectedAction applied to state $state")
  }

  // Syntax --->
  final case class --->(state: ActivityWorkflowState, action: ActivityWorkflowAction)
  implicit class WorkflowOps(state: ActivityWorkflowState) {
    def --->(action: ActivityWorkflowAction) = new --->(state, action)
  }

}
