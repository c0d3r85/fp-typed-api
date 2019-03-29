package ru.tinkoff.codefest.executor

import cats.data.NonEmptyList
import cats.{MonadError => ME}
import cats.syntax.functor._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._

import scala.tools.nsc.interpreter.IR

final class RemoteInterpretator[F[_]: ME[?[_], Throwable]: SttpBackend[?[_], Nothing]](remoteURI: String)
    extends Interpretator[F] {

  override def interpret(code: NonEmptyList[String]): F[Result] =
    sttp
      .post(uri"$remoteURI")
      .body(code.toList)
      .response(asJson[Result])
      .send()
      .map(_.unsafeBody)
      .map {
        case Right(res) => res
        case Left(_)    => Result(IR.Error, "", None)
      }

}
