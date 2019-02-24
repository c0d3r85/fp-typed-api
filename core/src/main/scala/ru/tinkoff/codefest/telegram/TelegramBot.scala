package ru.tinkoff.codefest.telegram
import com.bot4s.telegram.models.Update
import simulacrum.typeclass

@typeclass(generateAllOps = false) trait TelegramBot[F[_]] {

  def update(body: Update): F[Unit]

}
