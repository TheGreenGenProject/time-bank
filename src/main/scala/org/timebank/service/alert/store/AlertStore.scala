package org.timebank.service.alert.store

import org.timebank.core.{Page, UserId}
import org.timebank.service.alert.store.AlertStore.ReadStatus
import org.timebank.service.alert.{Alert, AlertId}

trait AlertStore[M[_]] {

  def byId(alertId: AlertId): M[Option[Alert]]

  def byUser(userId: UserId,
             status: ReadStatus,
             page: Page): M[List[Alert]]

  def count(userId: UserId,
            status: ReadStatus): M[Int]

  def mark(alertId: AlertId,
           userId: UserId,
           status: ReadStatus): M[Unit]

  def register(userId: UserId, alert: Alert): M[Unit]

}

object AlertStore {
  sealed trait ReadStatus
  case object All extends ReadStatus
  case object Unread extends ReadStatus
  case object Read extends ReadStatus
}