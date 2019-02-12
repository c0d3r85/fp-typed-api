package ru.tinkoff.codefest.http

import scala.concurrent.Future

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import cats.~>

import ru.tinkoff.tschema.akkaHttp.Routable

abstract class ApiModule[F[_]](implicit nt: F ~> Future) {

  implicit def routable[A](implicit m: ToResponseMarshaller[A]): Routable[F[A], A] =
    x => complete(nt(x))

  def route: Route

}
