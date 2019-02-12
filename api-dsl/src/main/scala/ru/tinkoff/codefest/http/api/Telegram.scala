package ru.tinkoff.codefest.http.api

import com.bot4s.telegram.models.Update
import simulacrum.typeclass

import ru.tinkoff.tschema.syntax.{prefix => tsprefix, _}

object Telegram {

  val routes = prefix :> tsprefix('telegram) {
    def auth = apiKeyAuth('token, queryParam[String]('token))

    operation('webhook) :> auth :> post :> reqBody[Update] :> $$[Unit]
  }

  @typeclass(generateAllOps = false) trait Controller[F[_]] {

    def webhook(token: String, body: Update): F[Unit]

  }

}
