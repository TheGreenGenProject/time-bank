package org.timebank.service.activity.store

import org.timebank.core.Page
import org.timebank.service.activity._


trait ActivityRequestStore[M[_]] {

  def byId(id: ActivityRequestId): M[Option[ActivityRequest]]

  def byIds(ids: List[ActivityRequestId]): M[List[ActivityRequest]]

  def byRequester(userId: Requester,
                  page: Page): M[List[ActivityRequest]]

  def byOwner(userId: Owner,
              page: Page): M[List[ActivityRequest]]

  def register(requester: Requester,
               activityRequest: ActivityRequest): M[Unit]

  def getState(activityRequestId: ActivityRequestId): M[Option[ActivityWorkflowState]]

  def moveState(activityRequestId: ActivityRequestId,
                state: ActivityWorkflowState): M[Unit]

}
