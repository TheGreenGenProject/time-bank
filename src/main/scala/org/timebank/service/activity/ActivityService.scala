package org.timebank.service.activity

import org.timebank.core.{Activity, ActivityId, Page, TimeSlot, UserId}


case class Owner(userId: UserId)
case class Requester(userId: UserId)

trait ActivityService[M[_]] {

  def byId(is: ActivityId): M[Option[Activity]]

  def byUser(userId: UserId, page: Page): M[List[Activity]]

  def isOwner(userId: UserId, activityId: ActivityId): M[Boolean]

  def newActivity(userId: Owner,
                  description: String,
                  timeSlots: List[TimeSlot]): M[ActivityId]

  def update(activityId: ActivityId,
             description: String,
             timeSlots: List[TimeSlot]): M[Unit]

  def enable(userId: Owner, id: ActivityId): M[Unit]

  def disable(userId: Owner, id: ActivityId): M[Unit]

  def checkActivity(id: ActivityId): M[Unit]

}
