package ru.tinkoff.codefest.http.api

import ru.tinkoff.codefest.executor.Result
import simulacrum.typeclass
import ru.tinkoff.tschema.syntax._

object Root {

  final case class Version(version: String)

  val routes =
    // format: off
    prefix :> {
      operation('version)   :> get                             :> $$[Version] <|>
      operation('interpret) :> post :> reqBody[Vector[String]] :> $$[Result]
    }
    // format: on

  @typeclass(generateAllOps = false) trait Controller[F[_]] {

    def version: F[Version]

    def interpret(body: Vector[String]): F[Result]
  }

}
