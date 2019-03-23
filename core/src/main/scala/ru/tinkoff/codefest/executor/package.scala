package ru.tinkoff.codefest
import io.circe.{Decoder, Encoder}
import io.circe.generic.JsonCodec

import scala.tools.nsc.interpreter.IR

package object executor {

  @JsonCodec
  final case class Result(status: IR.Result, output: String, compiled: Option[String])

  object Result {
    implicit val d: Decoder[IR.Result] = Decoder.decodeString.map {
      case "success"    => IR.Success
      case "error"      => IR.Error
      case "incomplete" => IR.Incomplete
    }
    implicit val e: Encoder[IR.Result] =
      Encoder.encodeString.contramap[IR.Result](_.getClass.getSimpleName.toLowerCase)
  }

  final case class IRState(
      status: IR.Result = IR.Success,
      compiled: Option[String] = None,
      incompleteBuffer: Option[String] = None
  )
}
