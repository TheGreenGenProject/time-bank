package org.timebank.service.transaction

import org.timebank.core._
import scala.concurrent.duration.Duration


trait TransactionService[M[_]] {

  def byId(transactionId: TransactionId): M[Option[Transaction]]

  def account(user: UserId): M[Option[AccountId]]

  def balance(userId: UserId): M[Option[HourPayment]]

  def balance(account: AccountId): M[Option[HourPayment]]

  // Performs a transaction between 2 accounts
  def transact(payer: AccountId,
               receiver: AccountId,
               amount: HourPayment): M[TransactionId]

  // Reserve a transaction - this can be cancelled or transferred
  // Amount is taken out of the payer - but not released to the payee until validation
  def reserve(payer: AccountId,
              receiver: AccountId,
              amount: HourPayment): M[ReservationId]

  def release(reservationId: ReservationId): M[Unit]

  def cancel(reservationId: ReservationId): M[Unit]

  def transactions(id: AccountId,
                   page: Page): M[List[Transaction]]

  // Count the number of transactions in the last period
  def count(lastPeriod: Duration): M[Int]

}
