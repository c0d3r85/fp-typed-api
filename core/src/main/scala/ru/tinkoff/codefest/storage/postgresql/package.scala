package ru.tinkoff.codefest.storage

import io.getquill.{PostgresAsyncContext, SnakeCase}

package object postgresql {
  val ctx = new PostgresAsyncContext(SnakeCase, "ru.tinkoff.codefest.postgresql")

  implicit class ChatId(val underlying: Long) extends AnyVal

  final case class Chat(chatId: ChatId, state: Option[String] = None)

  final case class Snippet(id: String, code: String)
}
