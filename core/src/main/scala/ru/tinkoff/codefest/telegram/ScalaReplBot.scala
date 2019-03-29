package ru.tinkoff.codefest.telegram

import cats.Monad
import cats.data.{NonEmptyList, OptionT}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Update
import ru.tinkoff.codefest.executor.{Interpretator, Result}
import ru.tinkoff.codefest.storage.Storage
import ru.tinkoff.codefest.storage.postgresql.Snippet

import scala.tools.nsc.interpreter.IR

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
        case BotCommand(Start | Help)               => help(chatId)
        case BotCommand(Reset)                      => reset(chatId)
        case BotCommand(State)                      => state(chatId)
        case snippetId if snippetId.startsWith("/") => snippet(chatId, snippetId.drop(1))
        case _                                      => interpret(chatId, text)
      })
    } yield cmd
    maybeUnit.getOrElseF(F.unit)
  }

  private def snippet(chatId: Long, snippetId: String): F[Unit] =
    storage
      .snippet(snippetId)
      .flatMap {
        case Some(Snippet(_, code)) =>
          val msg = SendMessage(chatId = chatId, text = s"Snippet $snippetId\n\n$code")
          for {
            _ <- RequestHandler[F].apply(msg)
            output <- Interpretator[F].interpret(NonEmptyList.of(code)).map(_.output)
          } yield output
        case None => F.pure("<unknown command>")
      }
      .map(SendMessage(chatId = chatId, _))
      .flatMap(msg => RequestHandler[F].apply(msg).map(_ => ()))

  private def interpret(chatId: Long, text: String): F[Unit] =
    for {
      state <- storage.load(chatId)
      newState = NonEmptyList.of(text, state.toList: _*)
      result <- Interpretator[F].interpret(newState)
      text = result match {
        case Result(IR.Incomplete, _, _)                 => "<incomplete>" //fixme
        case Result(_, output, _) if output.trim.isEmpty => "<empty>" //fixme
        case Result(_, output, _)                        => output
      }
      _ <- RequestHandler[F].apply(SendMessage(chatId = chatId, text = text))
      _ <- storage.save(chatId, result.compiled)
    } yield F.unit

  private def help(chatId: Long): F[Unit] =
    RequestHandler[F].apply(SendMessage(chatId = chatId, text = ScalaReplBot.help)).map(_ => F.unit)

  private def reset(chatId: Long): F[Unit] =
    for {
      _ <- storage.save(chatId, None)
      _ <- RequestHandler[F].apply(SendMessage(chatId = chatId, text = "OK!"))
    } yield F.unit

  private def state(chatId: Long): F[Unit] =
    for {
      s <- storage.load(chatId).map(_.getOrElse("<empty state>"))
      _ <- RequestHandler[F].apply(SendMessage(chatId = chatId, text = s))
    } yield F.unit
}

object ScalaReplBot {
  val help =
    """
      |Hello, I am ScaREBot
      |My goal is to interpret your scala code!
    """.stripMargin
}
