package org.timebank.service.activity.impl

import cats.effect.IO
import org.timebank.core._
import org.timebank.service.activity.store.ActivityStore
import org.timebank.service.activity.{ActivityService, Owner}


class ActivityServiceImpl(clock: Clock, idGenerator: UUIDGenerator)
                         (store: ActivityStore[IO])
  extends ActivityService[IO] {

  override def byId(id: ActivityId): IO[Option[Activity]] =
    store.byId(id)

  override def byUser(userId: UserId,
                      page: Page): IO[List[Activity]] =
    store.byOwner(Owner(userId), page)

  override def isOwner(userId: UserId,
                       activityId: ActivityId): IO[Boolean] =
    for {
      maybeActivity <- byId(activityId)
      activity      <- IO.fromOption(maybeActivity)(new RuntimeException(s"No activity found dor id ${activityId}"))
    } yield activity.userId == userId

  override def newActivity(owner: Owner,
                           description: String,
                           timeSlots: List[TimeSlot]): IO[ActivityId] =
    for {
      newId <- IO(ActivityId(idGenerator.random()))
      activity = Activity(newId, owner.userId, description)
      _     <- store.register(activity)
      _     <- store.register(activity.id, timeSlots)
    } yield newId

  // FIXME subject to race conditions
  override def update(activityId: ActivityId,
                      description: String,
                      timeSlots: List[TimeSlot]): IO[Unit] =
    for {
      maybeActivity <- store.byId(activityId)
      activity      <- IO.fromOption(maybeActivity)(new RuntimeException(s"Couldn't find activity with id ${activityId}"))
      newActivity = activity.copy(description = description)
      _             <- store.register(newActivity)
      _             <- store.register(activity.id, timeSlots)
    } yield ()

  override def enable(userId: Owner,
                      id: ActivityId): IO[Unit] =
    for {
      _ <- checkOwner(userId.userId, id)
      _ <- store.enable(id)
    } yield ()

  override def disable(userId: Owner,
                       id: ActivityId): IO[Unit] =
    for {
      _ <- checkOwner(userId.userId, id)
      _ <- store.disable(id)
    } yield ()

  override def checkActivity(id: ActivityId): IO[Unit] =
    for {
      enabled <- store.isEnabled(id)
      _       <- IO.raiseWhen(!enabled)(new RuntimeException(s"Activity ${id} doesn't exist or is not enabled"))
    } yield ()

  // Helpers

  private def checkOwner(userId: UserId, activityId: ActivityId): IO[Unit] =
    for {
      owner <- isOwner(userId, activityId)
      _     <- IO.raiseWhen(!owner)(new RuntimeException(s"User ${userId} is not owner of activity ${activityId}"))
    } yield ()
}
