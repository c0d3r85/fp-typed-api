package ru.tinkoff.codefest.http.api

import ru.tinkoff.tschema.syntax._

object Root {

  final case class Version(version: String)

  val routes = prefix :> {
    operation('version) :> get :> $$[Version]
  }

  trait Controller[F[_]] {

    def version: F[Version]

  }

  object Controller {
    def apply[F[_]: Controller]: Controller[F] = implicitly[Controller[F]]
  }

}
