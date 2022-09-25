package org.timebank.service.timebank.store

import org.timebank.core.{Page, TimeBank, TimeBankId, TimeBankLink}
import org.timebank.service.timebank.{LinkRequestId, TimeBankLinkRequest}


trait TimeBankStore[M[_]] {

  def byId(timeBankId: TimeBankId): M[Option[TimeBank]]

  def byId(linkRequest: LinkRequestId): M[Option[TimeBankLinkRequest]]

  def register(timeBank: TimeBank): M[Unit]

  def enable(timeBankId: TimeBankId): M[Unit]

  def disable(timeBankId: TimeBankId): M[Unit]

  def isEnabled(timeBankId: TimeBankId): M[Boolean]

  def link(tbid1: TimeBankId, tbid2: TimeBankId): M[Unit]

  def reject(linkRequestId: LinkRequestId, reason: String): M[Unit]

  def unlink(tbid1: TimeBankId, tbid2: TimeBankId): M[Unit]

  def areLinked(tbid1: TimeBankId, tbid2: TimeBankId): M[Boolean]

  def requestLink(timeBankLinkRequest: TimeBankLinkRequest): M[Unit]

  def linkRequests(timeBankId: TimeBankId, page: Page): M[List[TimeBankLinkRequest]]

  def links(timeBankId: TimeBankId, page: Page): M[List[TimeBankLink]]

}
