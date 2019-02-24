package ru.tinkoff.codefest.telegram

import cats.Monad
import cats.data.OptionT
import cats.effect.Sync
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Update

import ru.tinkoff.codefest.executor.Interpretator

class ScalaReplBot[F[_]: RequestHandler: Interpretator: Monad: Sync] extends TelegramBot[F] { // FIXME: remove Sync dep

  override def update(body: Update): F[Unit] =
    (for {
      message <- OptionT.fromOption[F](body.message)
      text <- OptionT.fromOption[F](message.text)
      result <- OptionT.liftF(Interpretator[F].interpret(text.split("\n").toVector))
      _ <- OptionT.liftF(
        RequestHandler[F].apply(SendMessage(chatId = message.chat.id, text = result.mkString("\n")))
      )
    } yield ()).getOrElse(())

}
