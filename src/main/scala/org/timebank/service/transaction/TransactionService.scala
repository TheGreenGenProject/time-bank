package org.timebank.service.transaction

import org.timebank.core._

import scala.concurrent.duration.Duration

trait TransactionService[M[_]] {

  def byId(transactionId: TransactionId): M[Option[Transaction]]

  def account(user: UserId): M[Option[AccountId]]

  def balance(userId: UserId): M[Option[HourPayment]]

  def balance(account: AccountId): M[Option[HourPayment]]

  // Performs a transaction between 2 accounts
  def transact(payer: AccountId, receiver: AccountId, amount: HourPayment): M[Either[String, TransactionId]]

  def transactions(id: AccountId, page: Page): M[List[Transaction]]

  // Count the number of transactions in the last period
  def count(lastPeriod: Duration): M[Int]

}
