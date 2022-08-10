package org.timebank.service.alert

import org.timebank.core.{UTCTimestamp, UUID, UserId}

sealed trait AlertType
object AlertType {
  case class AdminAlert(sender: UserId, message: String) extends AlertType
  case class PlatformAlert(message: String) extends AlertType
  case class ActivityCancelled(canceller: UserId, message: String) extends AlertType
}

case class AlertId(id: UUID)
case class Alert(id: AlertId, alertType: AlertType, timestamp: UTCTimestamp)