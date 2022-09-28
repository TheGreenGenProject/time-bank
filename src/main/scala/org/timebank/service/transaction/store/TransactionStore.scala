package org.timebank.service.transaction.store

import org.timebank.core._


trait TransactionStore[M[_]] {

  def byId(transactionId: TransactionId): M[Option[Transaction]]

  def byId(accountId: AccountId): M[Option[Account]]

  def transactions(accountId: AccountId, page: Page): M[List[Transaction]]

  def accountByUserId(userId: UserId): M[Option[Account]]

  def balance(accountId: AccountId): M[HourPayment]

  def register(transaction: Transaction): M[Unit]

  def register(account: Account): M[Unit]

  def reserve(transaction: Transaction): M[ReservationId]

  def release(reservationId: ReservationId): M[Option[TransactionId]]

  def cancel(reservationId: ReservationId): M[Unit]

}
