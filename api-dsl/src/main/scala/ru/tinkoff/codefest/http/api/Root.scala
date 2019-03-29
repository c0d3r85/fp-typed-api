package ru.tinkoff.codefest.http.api

import enumeratum._
import enumeratum.EnumEntry.Lowercase
import io.circe.generic.JsonCodec
import ru.tinkoff.tschema.swagger.{SwaggerEnumeration, SwaggerTypeable}
import simulacrum.typeclass
import ru.tinkoff.tschema.syntax._

object Root {

  final case class Version(version: String)

  object Version {
    implicit val _: SwaggerTypeable[Version] = SwaggerTypeable.genTypeable[Version]
  }

  sealed trait Status extends EnumEntry

  object Status extends Enum[Status] with CirceEnum[Status] with Lowercase {
    val values = findValues

    case object Success extends Status
    case object Error extends Status
    case object Incomplete extends Status

    implicit val _: SwaggerTypeable[Status] =
      SwaggerTypeable.make[Status](SwaggerEnumeration(values.map(_.entryName.toLowerCase).toVector))
  }

  @JsonCodec
  final case class IRResponse(status: Status, output: String, compiled: Option[String])

  object IRResponse {
    implicit val _: SwaggerTypeable[IRResponse] = SwaggerTypeable.genNamedTypeable[IRResponse]("IRResponse")
  }

  val routes =
    // format: off
    prefix :> {
      operation('version)   :> get                             :> $$[Version] <|>
      operation('interpret) :> post :> reqBody[Vector[String]] :> $$[IRResponse]
    }
    // format: on

  @typeclass(generateAllOps = false) trait Controller[F[_]] {

    def version: F[Version]

    def interpret(body: Vector[String]): F[IRResponse]
  }

}
