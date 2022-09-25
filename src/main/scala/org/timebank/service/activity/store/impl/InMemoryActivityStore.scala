package org.timebank.service.activity.store.impl

import cats.effect.IO
import org.timebank.core.{Activity, ActivityId, Page, TimeSlot}
import org.timebank.service.activity.store.ActivityStore
import org.timebank.service.activity.{Owner, Requester}

import scala.collection.concurrent.TrieMap


class InMemoryActivityStore extends ActivityStore[IO] {

  private val activitiesById        = new TrieMap[ActivityId, Activity]
  private val activitiesByOwner     = new TrieMap[Owner, List[ActivityId]]
  private val activitiesByRequester = new TrieMap[Requester, List[ActivityId]]
  private val statusesById          = new TrieMap[ActivityId, Boolean]
  private val timeSlotsById          = new TrieMap[ActivityId, List[TimeSlot]]


  override def byId(activityId: ActivityId): IO[Option[Activity]] =
    IO(activitiesById.get(activityId))

  override def byIds(ids: List[ActivityId]): IO[List[Activity]] =
    IO(ids.flatMap(activitiesById.get(_)))

  override def isEnabled(id: ActivityId): IO[Boolean] =
    IO(activitiesById.contains(id) && statusesById.getOrElse(id, true))

  override def byOwner(owner: Owner, page: Page): IO[List[Activity]] =
    for {
      ids        <- IO(activitiesByOwner.getOrElse(owner, List()))
      activities <- byIds(ids)
      paginated = Page.get(page, activities)
    } yield paginated

  override def byOwnerAndStatus(owner: Owner,
                                status: Boolean,
                                page: Page): IO[List[Activity]] =
    for {
      ids <- IO(activitiesByOwner.getOrElse(owner, List()))
      idsForStatus = ids.filter(statusesById.getOrElse(_, true))
      activities <- byIds(idsForStatus)
      paginated = Page.get(page, activities)
    } yield paginated

  override def count(owner: Owner): IO[Int] =
    IO(activitiesByOwner.getOrElse(owner, List()).size)

  override def count(requester: Requester): IO[Int] =
    IO(activitiesByRequester.getOrElse(requester, List()).size)

  override def register(activity: Activity): IO[Unit] =
    IO(activitiesById.put(activity.id, activity)).void

  override def register(id: ActivityId,
                        slots: List[TimeSlot]): IO[Unit] =
    IO(timeSlotsById.put(id, slots)).void

  override def enable(id: ActivityId): IO[Unit] =
    IO(statusesById.put(id, true)).void

  override def disable(id: ActivityId): IO[Unit] =
    IO(statusesById.put(id, false)).void

}
