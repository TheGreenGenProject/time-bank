package org.timebank.service.timebank.impl

import cats.effect.IO
import org.timebank.core._
import org.timebank.service.timebank.store.TimeBankStore
import org.timebank.service.timebank.{LinkRequestId, TimeBankLinkRequest, TimeBankService}
import org.timebank.service.user.UserService



class TimeBankServiceImpl(clock: Clock,
                          uuidGenerator: UUIDGenerator,
                          store: TimeBankStore[IO],
                          userService: UserService[IO]) extends TimeBankService[IO] {

  override def byId(id: TimeBankId): IO[Option[TimeBank]] =
    store.byId(id)

  override def enable(admin: Admin, id: TimeBankId): IO[Unit] = for {
    _ <- userService.checkAdmin(admin)
    _ <- store.enable(id)
  } yield ()

  override def disable(admin: Admin, id: TimeBankId): IO[Unit] = for {
    _ <- userService.checkAdmin(admin)
    _ <- store.disable(id)
  } yield ()

  override def isEnabled(id: TimeBankId): IO[Boolean] =
    store.isEnabled(id)

  override def checkTimeBank(id: TimeBankId): IO[Unit] = for {
    maybeTimeBank <- byId(id)
    _             <- IO.fromOption(maybeTimeBank)(new RuntimeException(s"No TimeBank with id ${id} can be found"))
    enabled       <- isEnabled(id)
    _             <- IO.raiseWhen(!enabled)(new RuntimeException(s"TimeBank ${id} is not enabled"))
  } yield ()

  override def userCount(id: TimeBankId): IO[Int] = ???

  override def areLinked(id1: TimeBankId, id2: TimeBankId): IO[Boolean] =
    store.areLinked(id1, id2)

  override def requestLink(admin: Admin,
                           requester: TimeBankId,
                           requested: TimeBankId): IO[Unit] = for {
    _ <- IO.raiseWhen(requester == requested)(new RuntimeException(s"Invalid link request. Time bank ids must be different (${requester})"))
    _ <- userService.checkAdmin(admin)
    // FIXME check admin belongs to requester
    _ <- checkTimeBank(requester)
    _ <- checkTimeBank(requested)
    linkRequestId = LinkRequestId(uuidGenerator.random())
    request = TimeBankLinkRequest(linkRequestId, admin, requester, requested, clock.now())
    _ <- store.requestLink(request)
  } yield ()

  override def acceptLinkRequest(admin: Admin,
                                 id: LinkRequestId): IO[Unit] = for {
    _ <- userService.checkAdmin(admin)
    maybeRequest <- store.byId(id)
    request <- IO.fromOption(maybeRequest)(new RuntimeException(s"Cannot find link request with id ${id}"))
    _ <- store.link(request.requester, request.requested)
  } yield ()

  override def rejectLinkRequest(admin: Admin,
                                 id: LinkRequestId,
                                 reason: String): IO[Unit] = for {
    _ <- userService.checkAdmin(admin)
    maybeRequest <- store.byId(id)
    request <- IO.fromOption(maybeRequest)(new RuntimeException(s"Cannot find link request with id ${id}"))
    _ <- store.reject(id, reason)
  } yield ()

}
