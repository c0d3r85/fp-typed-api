package ru.tinkoff.codefest.telegram

import cats.Monad
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.option._
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Update

import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.storage.Storage

class ScalaReplBot[F[_]: RequestHandler: Storage: Interpretator: Monad: Sync]
    extends TelegramBot[F] { // FIXME: remove Sync dep

  override def update(body: Update): F[Unit] = {
    val storage = implicitly[Storage[F]]
    (for {
      message <- body.message.toOptionT[F]
      chatId = message.chat.id
      state <- OptionT.liftF(storage.load(chatId))
      text <- message.text.toOptionT[F]
      newState = Seq(state, text).mkString("\n")
      _ = println(newState)
      result <- OptionT.liftF(Interpretator[F].interpret(newState.split("\n").toVector))
      _ <- OptionT.liftF(
        RequestHandler[F].apply(SendMessage(chatId = message.chat.id, text = result.mkString("\n")))
      )
      _ <- OptionT.liftF(storage.save(chatId, newState.some))
    } yield ()).getOrElse(())
  }
}
