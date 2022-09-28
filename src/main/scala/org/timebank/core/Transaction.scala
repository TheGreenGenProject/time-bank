package org.timebank.core

case class HourPayment(value: Int)
case class AccountId(id: UUID)
case class Account(
  id: AccountId,
  owner: UserId,
  creation: UTCTimestamp,
  disabled: Option[UTCTimestamp])

case class Balance(accountId:AccountId, balance: HourPayment)

case class TransactionId(id: UUID)
case class Transaction(
  transactionId: TransactionId,
  timestamp: UTCTimestamp,
  payer: AccountId,
  receiver: AccountId,
  amount: HourPayment,
  comment: Option[String])

case class ReservationId(id: UUID)
case class Reservation(
  id: ReservationId,
  transactionId: TransactionId)
