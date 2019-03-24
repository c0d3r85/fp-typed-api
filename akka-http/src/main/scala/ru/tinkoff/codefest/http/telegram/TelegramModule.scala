package ru.tinkoff.codefest.http.telegram

import scala.concurrent.Future
import akka.http.scaladsl.server.Route
import cats.~>
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import ru.tinkoff.codefest.http.ApiModule
import ru.tinkoff.codefest.http.api.Telegram.{Controller, routes}
import ru.tinkoff.codefest.http.telegram.CirceDecoders._
import ru.tinkoff.tschema.akkaHttp.MkRoute
import ru.tinkoff.tschema.swagger.SwaggerBuilder

class TelegramModule[F[_]: Controller](implicit nt: F ~> Future) extends ApiModule[F] {

  override def route: Route = MkRoute(routes)(Controller[F])
  override def swagger: SwaggerBuilder = ???
}
