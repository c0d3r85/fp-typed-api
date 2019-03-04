package ru.tinkoff.codefest.telegram

import com.bot4s.telegram.models.Update
import simulacrum.typeclass

@typeclass(generateAllOps = false) trait TelegramBot[F[_]] {

  def webhook(body: Update): F[Unit]

}
