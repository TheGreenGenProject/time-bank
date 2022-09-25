package org.timebank.service.activity.store.impl

import cats.effect.IO
import org.timebank.core.Page
import org.timebank.service.activity.store.{ActivityRequestStore, ActivityStore}
import org.timebank.service.activity.{ActivityRequest, ActivityRequestId, ActivityWorkflowAction, ActivityWorkflowState, Owner, Requester}

import scala.collection.concurrent.TrieMap


class InMemoryActivityRequestStore(activityStore: ActivityStore[IO])
  extends ActivityRequestStore[IO] {

  private val requestsById        = new TrieMap[ActivityRequestId, ActivityRequest]
  private val requestsByRequester = new TrieMap[Requester, List[ActivityRequestId]]
  private val requestsByOwner     = new TrieMap[Owner, List[ActivityRequestId]]
  private val statesById          = new TrieMap[ActivityRequestId, ActivityWorkflowState]


  override def byId(id: ActivityRequestId): IO[Option[ActivityRequest]] =
    IO(requestsById.get(id))

  override def byIds(ids: List[ActivityRequestId]): IO[List[ActivityRequest]] =
    IO(ids.flatMap(requestsById.get(_)))

  override def byRequester(requester: Requester,
                           page: Page): IO[List[ActivityRequest]] =
    for {
      ids <- IO(requestsByRequester.getOrElse(requester, List()))
      requests <- byIds(ids)
      paginated = Page.get(page, requests)
    } yield paginated

  override def byOwner(owner: Owner,
                       page: Page): IO[List[ActivityRequest]] =
    for {
      ids <- IO(requestsByOwner.getOrElse(owner, List()))
      requests <- byIds(ids)
      paginated = Page.get(page, requests)
    } yield paginated

  override def register(requester: Requester,
                        activityRequest: ActivityRequest): IO[Unit] =
    for {
      maybeActivity <- activityStore.byId(activityRequest.activity)
      activity <- IO.fromOption(maybeActivity)(new RuntimeException(s"Cannot find activity ${activityRequest.activity}"))
      owner = Owner(activity.userId)
      _ = requestsById.put(activityRequest.id, activityRequest)
      _ = requestsByOwner.updateWith(owner) {
        case Some(xs) => Some((activityRequest.id :: xs).distinct)
        case None     => Some(List(activityRequest.id))
      }
      _ = requestsByRequester.updateWith(requester) {
        case Some(xs) => Some((activityRequest.id :: xs).distinct)
        case None     => Some(List(activityRequest.id))
      }
    } yield ()

  override def getState(activityRequestId: ActivityRequestId): IO[Option[ActivityWorkflowState]] =
    IO(statesById.get(activityRequestId))

  override def moveState(activityRequestId: ActivityRequestId,
                         state: ActivityWorkflowState): IO[Unit] =
    IO(statesById.put(activityRequestId, state)).void

}
