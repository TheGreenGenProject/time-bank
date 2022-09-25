package org.timebank.service.user

import org.timebank.core.{Admin, Page, TimeBankId, UserId, ValidationMethod}


trait UserService[M[_]] {

  def byId(userId: UserId): M[Option[UserId]]

  def isEnabled(userId: UserId): M[Boolean]

  def checkUser(userId: UserId): M[Unit]

  def users(page: Page): M[List[UserId]]

  def isAdmin(userId: UserId): M[Boolean]

  def checkAdmin(admin: Admin): M[Boolean]

  def admins(page: Page): M[List[Admin]]

  def createUser(timeBankId: TimeBankId,
                 name: String,
                 description: Option[String]): M[Either[String, ValidationMethod]]

  def enable(admin: Admin,
             userId: UserId,
             reason: String): M[Unit]

  def disable(admin: Admin,
              userId: UserId,
              reason: String): M[Unit]

}
