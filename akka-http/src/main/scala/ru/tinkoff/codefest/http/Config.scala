package ru.tinkoff.codefest.http

import cats.ApplicativeError
import io.circe.config.parser
import io.circe.generic.auto._
import simulacrum.typeclass

final case class WebConfig(port: Int)

final case class TelegramConfig(token: String, webhook: String)

final case class Config(web: WebConfig, telegram: TelegramConfig)

@typeclass(generateAllOps = false) trait ConfigModule[F[_]] {

  def load: F[Config]

}

class ConfigModuleImpl[F[_]: ApplicativeError[?[_], Throwable]]
    extends ConfigModule[F] {

  override def load: F[Config] = parser.decodeF[F, Config]

}
