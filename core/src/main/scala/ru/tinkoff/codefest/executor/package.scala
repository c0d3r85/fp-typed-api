package ru.tinkoff.codefest
import io.circe.Decoder
import io.circe.generic.JsonCodec

import scala.tools.nsc.interpreter.IR

package object executor {

  @JsonCodec(decodeOnly = true)
  final case class Result(status: IR.Result, output: String, compiled: Option[String])

  object Result {
    implicit val d: Decoder[IR.Result] = Decoder.decodeString.map {
      case "success"    => IR.Success
      case "error"      => IR.Error
      case "incomplete" => IR.Incomplete
    }
  }

  final case class IRState(
      status: IR.Result = IR.Success,
      compiled: Option[String] = None,
      incompleteBuffer: Option[String] = None
  )
}
