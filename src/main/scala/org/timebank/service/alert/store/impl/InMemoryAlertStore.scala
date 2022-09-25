package org.timebank.service.alert.store.impl

import cats.effect.IO
import cats.implicits._
import org.timebank.core.{Page, UserId}
import org.timebank.service.alert.{Alert, AlertId}
import org.timebank.service.alert.store.AlertStore

import scala.collection.concurrent.TrieMap



class InMemoryAlertStore extends AlertStore[IO] {

  private[this] val alertsById = new TrieMap[AlertId, Alert]
  private[this] val alertsByUsers = new TrieMap[UserId, List[AlertId]]
  private[this] val statusesByIdAndUser = new TrieMap[(AlertId, UserId), AlertStore.ReadStatus]

  override def byId(alertId: AlertId): IO[Option[Alert]] =
    IO(alertsById.get(alertId))

  override def byUser(userId: UserId,
                      status: AlertStore.ReadStatus,
                      page: Page): IO[List[Alert]] =
    for {
      alerts <- alertsByUsers.getOrElse(userId, List())
        .map(byId(_))
        .sequence
      filtered = alerts
        .collect { case Some(alert) if matchStatus(alert.id, userId, status) => alert }
        .sortBy(-1L * _.timestamp.value)
      paged = Page.get(page, filtered)
    } yield paged

  override def count(userId: UserId,
                     status: AlertStore.ReadStatus): IO[Int] =
    for {
      alerts <- alertsByUsers.getOrElse(userId, List())
        .map(byId(_))
        .sequence
      res = alerts.count {
        case Some(x) => matchStatus(x.id, userId, status)
        case None    => false
      }
    } yield res

  override def mark(alertId: AlertId,
                    userId: UserId,
                    status: AlertStore.ReadStatus): IO[Unit] =
    for {
      maybeAlert <- byId(alertId)
      alert      <- IO.fromOption(maybeAlert)(new RuntimeException(s"Couldn't find alert $alertId"))
      _ = statusesByIdAndUser.put((alert.id, userId), status)
    } yield ()

  override def register(userId: UserId, alert: Alert): IO[Unit] = for {
    _ <- IO(alertsById.put(alert.id, alert))
    _ <- IO(alertsByUsers.updateWith(userId ) {
      case Some(xs) => Some(alert.id :: xs)
      case None => Some(List(alert.id))
    })
    _ <- IO(statusesByIdAndUser.put((alert.id, userId), AlertStore.Unread))
  } yield ()


  // Helpers

  private[this] def alertStatus(id: AlertId, userId: UserId): AlertStore.ReadStatus =
    statusesByIdAndUser.getOrElse((id, userId), AlertStore.Unread)

  private[this] def matchStatus(id: AlertId, userId: UserId, status: AlertStore.ReadStatus): Boolean =
    status==AlertStore.All || alertStatus(id, userId) == status

}
