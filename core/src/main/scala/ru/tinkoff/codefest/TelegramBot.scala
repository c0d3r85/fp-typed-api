package ru.tinkoff.codefest

import simulacrum.typeclass

import com.bot4s.telegram.models.Update

@typeclass(generateAllOps = false) trait TelegramBot[F[_]] {

  def update(body: Update): F[Unit]

}
