package ru.tinkoff.codefest.storage
import ru.tinkoff.codefest.storage.postgresql.Snippet

trait Storage[F[_]] {
  def save(chatId: Long, state: Option[String]): F[Unit]
  def load(chatId: Long): F[Option[String]]
  def snippet(snippetId: String): F[Option[Snippet]]
}
