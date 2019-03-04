package ru.tinkoff.codefest.telegram

import cats.Monad
import cats.data.{NonEmptyList, OptionT}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Update

import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.storage.Storage

class ScalaReplBot[F[_]: RequestHandler: Storage: Interpretator: Monad: Sync]
    extends TelegramBot[F] { // FIXME: remove Sync dep

  private val storage = implicitly[Storage[F]]

  private val F = implicitly[Monad[F]]

  override def webhook(body: Update): F[Unit] = {
    import BotCommand._
    val maybeUnit = for {
      message <- body.message.toOptionT[F]
      text <- message.text.toOptionT[F]
      chatId = message.chat.id
      cmd <- OptionT.liftF(text match {
        case BotCommand(Reset) => reset(chatId)
        case BotCommand(State) => state(chatId)
        case _                 => interpret(chatId, text)
      })
    } yield cmd
    maybeUnit.getOrElseF(F.unit)
  }

  private def interpret(chatId: Long, text: String): F[Unit] =
    for {
      state <- storage.load(chatId)
      newState = NonEmptyList.of(text, state.toList: _*)
      result <- Interpretator[F].interpret(newState)
      _ <- RequestHandler[F].apply(SendMessage(chatId = chatId, text = result))
      _ <- storage.save(chatId, newState.reverse.toList.mkString("\n").some)
    } yield F.unit

  private def reset(chatId: Long): F[Unit] =
    for {
      _ <- storage.save(chatId, None)
      _ <- RequestHandler[F].apply(SendMessage(chatId = chatId, text = "OK!"))
    } yield F.unit

  private def state(chatId: Long): F[Unit] =
    for {
      s <- storage.load(chatId).map(_.getOrElse(""))
      _ <- RequestHandler[F].apply(SendMessage(chatId = chatId, text = s))
    } yield F.unit
}
