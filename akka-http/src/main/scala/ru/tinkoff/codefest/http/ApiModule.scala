package ru.tinkoff.codefest.http

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import cats.~>
import ru.tinkoff.tschema.akkaHttp.Routable

import scala.concurrent.Future

abstract class ApiModule[F[_]](implicit nt: F ~> Future) {

  implicit def routable[A](
      implicit m: ToResponseMarshaller[A]): Routable[F[A], A] =
    x => complete(nt(x))

  def route: Route

}
