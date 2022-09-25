package org.timebank.service.user.store

import org.timebank.core.{Admin, Page, User, UserId}

trait UserStore[M[_]] {

  def register(user: User): M[UserId]

  def byId(userId: UserId): M[Option[User]]

  def byIds(ids: List[UserId]): M[List[User]]

  def enable(admin: Admin, userId: UserId, reason: String): M[Unit]

  def disable(admin: Admin, userId: UserId, reason: String): M[Unit]

  def isEnabled(userId: UserId): M[Boolean]

  def admins(page: Page): M[List[Admin]]

  def isAdmin(userId: UserId): M[Boolean]

  def promote(admin: Admin, userId: UserId): M[Unit]

}
