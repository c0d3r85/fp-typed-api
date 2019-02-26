package ru.tinkoff.codefest.http.api

import simulacrum.typeclass

import ru.tinkoff.tschema.syntax._

object Root {

  final case class Version(version: String)

  val routes = prefix :> {
    operation('version) :> get :> $$[Version] <|>
      operation('interpret) :> post :> reqBody[Vector[String]] :> $$[
        Vector[String]]
  }

  @typeclass(generateAllOps = false) trait Controller[F[_]] {

    def version: F[Version]

    def interpret(body: Vector[String]): F[Vector[String]]
  }

}
