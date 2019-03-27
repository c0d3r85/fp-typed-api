package ru.tinkoff.codefest.storage.postgresql

package object queries {
  import ctx._
  def findByIdQuery(chatId: ChatId) = quote {
    query[Chat].filter(_.chatId == lift(chatId))
  }

  def upsertChatQuery(chat: Chat) = quote {
    query[Chat].insert(lift(chat)).onConflictUpdate(_.chatId)((t, e) => t.state -> e.state)
  }

  def findSnippetById(snippetId: String) = quote {
    query[Snippet].filter(_.id == lift(snippetId))
  }
}
