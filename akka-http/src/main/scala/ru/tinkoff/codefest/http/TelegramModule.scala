package ru.tinkoff.codefest.http

import scala.concurrent.Future

import com.bot4s.telegram.marshalling.CirceDecoders
import akka.http.scaladsl.server.Route
import cats.~>
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import ru.tinkoff.codefest.http.api.Telegram.{Controller, routes}
import ru.tinkoff.tschema.akkaHttp.MkRoute

class TelegramModule[F[_]: Controller](implicit nt: F ~> Future) extends ApiModule[F] with CirceDecoders {

  // TODO: need setWebhook call

  override def route: Route = MkRoute(routes)(Controller[F])

}
