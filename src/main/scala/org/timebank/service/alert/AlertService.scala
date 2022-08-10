package org.timebank.service.alert

import org.timebank.core.{Page, UserId}

trait AlertService[M[_]] {

  def alert(alert: Alert, user: UserId): M[Unit]
  def byUser(user: UserId, page: Page): M[List[Alert]]
  def byId(id: AlertId): Option[Alert]

}
