package ru.tinkoff.codefest.storage

import scala.language.higherKinds

trait Storage[F[_]] {
  def save(chatId: Long, state: Option[String]): F[Unit]
  def load(chatId: Long): F[Option[String]]
}
