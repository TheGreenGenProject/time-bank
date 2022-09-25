package org.timebank.service.transaction.impl

import cats.effect.IO
import org.timebank.core.{Account, AccountId, Clock, HourPayment, Page, ReservationId, Transaction, TransactionId, UTCTimestamp, UUID, UUIDGenerator, UserId}
import org.timebank.service.transaction.{Payer, Receiver, TransactionService}
import org.timebank.service.transaction.store.TransactionStore

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration


case class LockId(id: UUID)
case class Lock(id: LockId, accountId: AccountId, timestamp: UTCTimestamp)


class TransactionServiceImpl(clock: Clock,
                             uuidGenerator: UUIDGenerator,
                             store: TransactionStore[IO]) extends TransactionService[IO] {

  private val locksById = new TrieMap[LockId, Lock]
  private val locksByAccount = new TrieMap[AccountId, LockId]


  override def byId(transactionId: TransactionId): IO[Option[Transaction]] =
    store.byId(transactionId)

  override def account(user: UserId): IO[Option[Account]] =
    store.accountByUserId(user)

  override def balance(userId: UserId): IO[Option[HourPayment]] = ???

  override def balance(account: AccountId): IO[Option[HourPayment]] = ???

  override def transact(payer: Payer,
                        receiver: Receiver,
                        amount: HourPayment): IO[TransactionId] = for {
    lockId <- lock(payer.accountId)
    _      <- checkBalance(lockId, payer, amount)
    tid = TransactionId(uuidGenerator.random())
    trx = Transaction(tid, payer.accountId, receiver.accountId, amount, None)
    _      <- store.register(trx)
    _      <- unlock(lockId)
  } yield tid

  override def reserve(payer: Payer,
                       receiver: Receiver,
                       amount: HourPayment): IO[ReservationId] = for {
    lockId <- lock(payer.accountId)
    _ <- checkBalance(lockId, payer, amount)
    tid = TransactionId(uuidGenerator.random())
    trx = Transaction(tid, payer.accountId, receiver.accountId, amount, None)
    rid <- store.reserve(trx)
    _ <- unlock(lockId)
  } yield rid

  override def release(reservationId: ReservationId): IO[Unit] = ???

  override def cancel(reservationId: ReservationId): IO[Unit] = ???

  override def transactions(id: AccountId,
                            page: Page): IO[List[Transaction]] = ???

  override def count(lastPeriod: Duration): IO[Int] = ???


  // Helpers

  private[this] def checkBalance(lockId: LockId, payer: Payer, amount: HourPayment): IO[Unit] =
    for {
      _            <- checkLocked(lockId, payer.accountId)
      maybeBalance <- balance(payer.accountId)
      hours        <- IO.fromOption(maybeBalance)(new RuntimeException(s"Couldn't find a balance for $payer"))
      _            <- IO.raiseWhen(hours.value < amount.value)(new RuntimeException(s"Not enough balance on account $payer (${hours.value})"))
    } yield ()


  // Locking helpers

  private[this] def checkLocked(lockId: LockId, accountId: AccountId): IO[Unit] =
    IO.raiseWhen(!locksById.get(lockId).exists(_.accountId==accountId))(
      new RuntimeException(s"Lock id ${lockId} doesn't exist or is not locking account $accountId"))

  private[this] def lock(accountId: AccountId): IO[LockId] =
    for {
      lockId <- IO(LockId(uuidGenerator.random()))
      _ <- IO.raiseWhen(locksByAccount.putIfAbsent(accountId, lockId).nonEmpty)(
        new RuntimeException(s"Account $accountId is already locked"))
      _ <- IO(locksById.put(lockId, Lock(lockId, accountId, clock.now())))
    } yield lockId

  private[this] def unlock(id: LockId): IO[Unit] =
    for {
      maybeLock <- IO(locksById.get(id))
      lock      <- IO.fromOption(maybeLock)(new RuntimeException(s"Lock ${id} doesn't exists"))
      _         <- IO(locksByAccount.remove(lock.accountId))
    } yield lock
}
