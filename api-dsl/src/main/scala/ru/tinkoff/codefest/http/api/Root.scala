package ru.tinkoff.codefest.http.api

import ru.tinkoff.codefest.executor.Interpretator.Result
import simulacrum.typeclass
import ru.tinkoff.tschema.syntax._

object Root {

  final case class Version(version: String)

  val routes = prefix :> {
    operation('version) :> get :> $$[Version] <|>
      operation('interpret) :> post :> reqBody[Vector[String]] :> $$[Result]
  }

  @typeclass(generateAllOps = false) trait Controller[F[_]] {

    def version: F[Version]

    def interpret(body: Vector[String]): F[Result]
  }

}
