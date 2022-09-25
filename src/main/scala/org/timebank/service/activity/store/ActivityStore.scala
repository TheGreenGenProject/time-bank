package org.timebank.service.activity.store

import org.timebank.core.{Activity, ActivityId, Page, TimeSlot}
import org.timebank.service.activity.{Owner, Requester}


trait ActivityStore[M[_]] {

  def byId(activityId: ActivityId): M[Option[Activity]]

  def byIds(ids: List[ActivityId]): M[List[Activity]]

  def isEnabled(id: ActivityId): M[Boolean]

  def byOwner(userId: Owner, page: Page): M[List[Activity]]

  def byOwnerAndStatus(userId: Owner,
                       status: Boolean,
                       page: Page): M[List[Activity]]

  def count(userId: Owner): M[Int]

  def count(userId: Requester): M[Int]


  def register(activity: Activity): M[Unit]

  def register(id: ActivityId,
               slots: List[TimeSlot]): M[Unit]

  def enable(id: ActivityId): M[Unit]

  def disable(id: ActivityId): M[Unit]

}
