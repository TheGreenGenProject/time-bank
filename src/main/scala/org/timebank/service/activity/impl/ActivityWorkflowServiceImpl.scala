package org.timebank.service.activity.impl

import cats.effect.IO
import org.timebank.core.{ActivityId, Clock, TimeSlot, UUIDGenerator, UserId}
import org.timebank.service.activity.store.ActivityRequestStore
import org.timebank.service.activity._
import org.timebank.service.user.UserService


class ActivityWorkflowServiceImpl(clock: Clock, idGenerator: UUIDGenerator)
                                 (store: ActivityRequestStore[IO],
                                  userService: UserService[IO],
                                  activityService: ActivityService[IO])
  extends ActivityWorkflowService[IO] {

  private val moveState = ActivityWorkflowStateMachine.handle(clock) _

  override def newRequest(requester: Requester,
                          activityId: ActivityId,
                          timeSlots: List[TimeSlot],
                          message: Option[String]): IO[ActivityRequestId] =
    for {
      _ <- userService.checkUser(requester.userId)
      _ <- activityService.checkActivity(activityId)
      requestId = ActivityRequestId(idGenerator.random())
      now = clock.now()
      validUntil = Clock.plusDays(now, 7)
      request = ActivityRequest(requestId, requester, activityId, now, validUntil, timeSlots, message.getOrElse("N/A"))
      _ <- store.register(requester, request)
      _ <- store.moveState(requestId, Init(requestId, now))
    } yield request.id

  override def rejectRequest(requestId: ActivityRequestId,
                             organizer: Owner,
                             reason: Option[String]): IO[Unit] =
    for {
      state  <- checkRequestAndGetState(requestId, organizer)
      action = Reject(state.requestId, clock.now(), organizer, reason)
      _      <- processAction(requestId, state, action)
    } yield ()

  override def acceptRequest(requestId: ActivityRequestId,
                             organizer: Owner,
                             timeSlot: TimeSlot): IO[Unit] =
    for {
      state  <- checkRequestAndGetState(requestId, organizer)
      action = Accept(state.requestId, clock.now(), organizer, timeSlot)
      _      <- processAction(requestId, state, action)
    } yield ()

  override def cancelRequest(requestId: ActivityRequestId,
                             userId: UserId,
                             reason: Option[String]): IO[Unit] =
    for {
      state  <- checkRequestAndGetState(requestId)
      action = Cancel(state.requestId, clock.now(), userId, reason)
      _      <- processAction(requestId, state, action)
    } yield ()

  override def ready(requestId: ActivityRequestId,
                     organizer: Owner): IO[Unit] =
    for {
      state  <- checkRequestAndGetState(requestId, organizer)
      action = ActivityReady(state.requestId, clock.now(), organizer)
      _      <- processAction(requestId, state, action)
    } yield ()

  override def organizerValidation(requestId: ActivityRequestId,
                                   organizer: Owner): IO[Unit] =
    for {
      state  <- checkRequestAndGetState(requestId, organizer)
      action = OrganizerValidate(state.requestId, clock.now(), organizer)
      _      <- processAction(requestId, state, action)
    } yield ()

  override def requesterValidation(requestId: ActivityRequestId,
                                   requester: Requester): IO[Unit] =
    for {
      state  <- checkRequestAndGetState(requestId, requester)
      action = RequesterValidate(state.requestId, clock.now(), requester)
      _      <- processAction(requestId, state, action)
    } yield ()


  // Helpers

  private[this] def processAction(requestId: ActivityRequestId,
                                  state: ActivityWorkflowState,
                                  action: ActivityWorkflowAction): IO[Unit] =
    for {
      newState <- IO(moveState(state)(action))
      _        <- store.moveState(requestId, newState)
    } yield ()

  private[this] def checkRequestAndGetState(requestId: ActivityRequestId,
                                            organizer: Owner): IO[ActivityWorkflowState] =
    for {
      _            <- userService.checkUser(organizer.userId)
      maybeRequest <- store.byId(requestId)
      request      <- IO.fromOption(maybeRequest)(new RuntimeException(s"Couldn't find request with id ${requestId}"))
      isOwner      <- activityService.isOwner(organizer.userId, request.activity)
      _            <- IO.raiseWhen(!isOwner)(new RuntimeException(s"User ${organizer.userId} is not owner of activity ${request.activity}"))
      maybeState   <- store.getState(requestId)
      state        <- IO.fromOption(maybeState)(new RuntimeException(s"Couldn't retrieve state for request ${requestId}"))
    } yield state

  private[this] def checkRequestAndGetState(requestId: ActivityRequestId,
                                            requester: Requester): IO[ActivityWorkflowState] =
    for {
      _            <- userService.checkUser(requester.userId)
      maybeRequest <- store.byId(requestId)
      request      <- IO.fromOption(maybeRequest)(new RuntimeException(s"Couldn't find request with id ${requestId}"))
      _            <- IO.raiseWhen(request.requester != requester)(new RuntimeException(s"User ${requester.userId} is not Requester of activity ${request.activity}"))
      maybeState   <- store.getState(requestId)
      state        <- IO.fromOption(maybeState)(new RuntimeException(s"Couldn't retrieve state for request ${requestId}"))
    } yield state

  private[this] def checkRequestAndGetState(requestId: ActivityRequestId): IO[ActivityWorkflowState] =
    for {
      maybeRequest <- store.byId(requestId)
      _            <- IO.fromOption(maybeRequest)(new RuntimeException(s"Couldn't find request with id ${requestId}"))
      maybeState   <- store.getState(requestId)
      state        <- IO.fromOption(maybeState)(new RuntimeException(s"Couldn't retrieve state for request ${requestId}"))
    } yield state

}
