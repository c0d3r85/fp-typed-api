package ru.tinkoff.codefest.http

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import cats.~>
import io.circe.ObjectEncoder
import ru.tinkoff.codefest.http.api.Root.{Controller, Version, routes}
import ru.tinkoff.tschema.akkaHttp.MkRoute
import ru.tinkoff.tschema.swagger.MkSwagger

import scala.concurrent.Future

class RootModule[F[_]: Controller](implicit nt: F ~> Future)
    extends ApiModule[F]
    with RootJsonProtocol {

  override def route: Route = MkRoute(routes)(Controller[F])

  override def swagger = MkSwagger(routes)(())

}

trait RootJsonProtocol {

  import io.circe.generic.semiauto.deriveEncoder

  implicit val versionEncoder: ObjectEncoder[Version] = deriveEncoder[Version]

}
