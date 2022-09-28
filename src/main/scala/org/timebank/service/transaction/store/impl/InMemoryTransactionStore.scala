package org.timebank.service.transaction.store.impl

import cats.data.OptionT
import cats.effect.IO
import org.timebank.core._
import org.timebank.service.transaction.store.TransactionStore

import scala.collection.concurrent.TrieMap


class InMemoryTransactionStore(uuidGenerator: UUIDGenerator) extends TransactionStore[IO] {

  private[this] val transactionsById        = new TrieMap[TransactionId, Transaction]
  private[this] val accountsById            = new TrieMap[AccountId, Account]
  private[this] val accountIdsByUserId      = new TrieMap[UserId, AccountId]
  private[this] val transactionsByAccountId = new TrieMap[AccountId, List[Transaction]]
  private[this] val reservedTransactions    = new TrieMap[ReservationId, Transaction]

  override def byId(transactionId: TransactionId): IO[Option[Transaction]] =
    IO(transactionsById.get(transactionId))

  override def byId(accountId: AccountId): IO[Option[Account]] =
    IO(accountsById.get(accountId))

  override def transactions(accountId: AccountId, page: Page): IO[List[Transaction]] =
    for {
      transactions <- IO(transactionsByAccountId.getOrElse(accountId, List()))
      sorted = transactions.sortBy(-1L * _.timestamp.value)
      paged  = Page.get(page, sorted)
    } yield paged

  override def accountByUserId(userId: UserId): IO[Option[Account]] =
    (for {
      accountId <- OptionT.fromOption[IO](accountIdsByUserId.get(userId))
      account   <- OptionT(byId(accountId))
    } yield account).value

  override def balance(accountId: AccountId): IO[HourPayment] =
    for {
      maybeAccount <- byId(accountId)
      account      <- IO.fromOption(maybeAccount)(new RuntimeException(s"Couldn't retrieve account for account id $accountId"))
      transactions = transactionsByAccountId.getOrElse(account.id, List())
      amount       = transactions.foldLeft(0) {
        case (acc, trx) => acc + ((if(trx.receiver==accountId) 1 else -1) * trx.amount.value)
      }
    } yield HourPayment(amount)

  override def register(transaction: Transaction): IO[Unit] = IO {
    transactionsById.put(transaction.transactionId, transaction)
    transactionsByAccountId.updateWith(transaction.payer) {
      case Some(xs) => Some(transaction :: xs)
      case None     => Some(List(transaction))
    }
    transactionsByAccountId.updateWith(transaction.receiver) {
      case Some(xs) => Some(transaction :: xs)
      case None     => Some(List(transaction))
    }
  }.void

  override def register(account: Account): IO[Unit] = IO {
    accountsById.put(account.id, account)
    accountIdsByUserId.put(account.owner, account.id)
  }.void

  override def reserve(transaction: Transaction): IO[ReservationId] = IO {
    val reservationId = ReservationId(uuidGenerator.random())
    reservedTransactions.put(reservationId, transaction)
    reservationId
  }

  override def release(reservationId: ReservationId): IO[Option[TransactionId]] =
    (for {
      transaction <- OptionT.fromOption[IO](reservedTransactions.remove(reservationId))
      _           <- OptionT.liftF(register(transaction))
    } yield transaction.transactionId).value

  override def cancel(reservationId: ReservationId): IO[Unit] =
    IO(reservedTransactions.remove(reservationId)).void

}
