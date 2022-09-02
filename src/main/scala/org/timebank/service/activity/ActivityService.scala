package org.timebank.service.activity

import org.timebank.core.{Activity, ActivityId, Hashtag, Page, TimeSlot, UserId}


trait ActivityService[M[_]] {

  def byId(is: ActivityId): M[Option[Activity]]

  def byUser(userId: UserId, page: Page): M[List[Activity]]

  def isOwner(userId: UserId, activityId: ActivityId): M[Boolean]

  def newActivity(userId: UserId,
                  description: String,
                  timeSlot: TimeSlot,
                  hashtags: List[Hashtag]): M[ActivityId]

  def declareInterest(userId: UserId,
                      hashtags: List[Hashtag]): M[Unit]

  def update(activityId: ActivityId,
             description: String,
             timeSlot: TimeSlot,
             hashtags: List[Hashtag]): M[ActivityId]

  def enable(userId: UserId, id: ActivityId): M[Unit]

  def disable(userId: UserId, id: ActivityId): M[Unit]

}
