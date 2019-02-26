package ru.tinkoff.codefest.http.telegram

import cats.MonadError
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.monadError._
import com.bot4s.telegram.models.Update

import ru.tinkoff.codefest.http.api.Telegram.Controller
import ru.tinkoff.codefest.telegram.TelegramBot

class TelegramController[F[_]: TelegramBot: MonadError[?[_], Throwable]](telegramToken: String) // FIXME: ADT for exceptions
    extends Controller[F] {

  override def webhook(token: String, body: Update): F[Unit] =
    for {
      _ <- MonadError[F, Throwable]
        .pure(telegramToken)
        .ensure(new IllegalArgumentException("Invalid token"))(_ == token)
      _ <- TelegramBot[F].update(body)
    } yield ()

}
