package ru.tinkoff.codefest.storage.postgresql

import scala.concurrent.{ExecutionContext, Future}

import cats.~>
import scala.language.higherKinds
import ru.tinkoff.codefest.storage.Storage
import ru.tinkoff.codefest.storage.postgresql.queries._

class PostgreSQLStorage[F[_]](implicit ec: ExecutionContext, nt: Future ~> F) extends Storage[F] {
  import ctx._
  override def save(chatId: Long, state: Option[String]): F[Unit] =
    nt(ctx.run(upsertChatQuery(Chat(chatId, state))).map(_ => ()))

  override def load(chatId: Long): F[Option[String]] =
    nt(ctx.run(findByIdQuery(chatId)).map(_.headOption.flatMap(_.state)))
}
