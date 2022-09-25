package org.timebank.service.alert.impl

import cats.effect.IO
import org.timebank.core.{Page, UserId}
import org.timebank.service.alert.{Alert, AlertId, AlertService}
import org.timebank.service.alert.store.AlertStore


class AlertServiceImpl(store: AlertStore[IO]) extends AlertService[IO] {

  override def post(alert: Alert, user: UserId): IO[Unit] =
    store.register(user, alert)

  override def byUser(user: UserId, page: Page): IO[List[Alert]] =
    store.byUser(user, AlertStore.All, page)

  override def byId(id: AlertId): IO[Option[Alert]] =
    store.byId(id)
}
