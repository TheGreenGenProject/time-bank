package org.timebank.core

case class HourPayment(value: Int)
case class AccountId(id: UUID)
case class Account(
  id: AccountId,
  owner: UserId,
  balance: HourPayment,
  creation: UTCTimestamp,
  disabled: Option[UTCTimestamp])

case class TransactionId(id: UUID)
case class Transaction(
  timeBankId: TimeBankId,
  payer: AccountId,
  receiver: AccountId,
  amount: HourPayment,
  comment: Option[String])

case class ReservationId(id: UUID)
case class Reservation(
  id: ReservationId,
  transactionId: TransactionId)
