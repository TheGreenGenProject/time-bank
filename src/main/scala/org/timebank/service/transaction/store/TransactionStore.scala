package org.timebank.service.transaction.store

import org.timebank.core._


trait TransactionStore[M[_]] {

  def byId(transactionId: TransactionId): M[Option[Transaction]]

  def byId(accountId: AccountId): M[Option[Account]]

  def transactions(userId: UserId, page: Page): M[List[Transaction]]

  def accountByUserId(userId: UserId): M[Option[Account]]

  def register(transaction: Transaction): M[Unit]

  def register(account: Account): M[Unit]

  def reserve(transaction: Transaction): M[ReservationId]

  def release(reservationId: ReservationId): M[Unit]

  def cancel(reservationId: ReservationId): M[Unit]

}
