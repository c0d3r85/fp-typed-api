package ru.tinkoff.codefest.http

import cats.Monad
import cats.data.{NonEmptyList, NonEmptyVector, OptionT}
import cats.syntax.applicative._
import cats.syntax.option._
import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.executor.Interpretator.Result
import ru.tinkoff.codefest.http.api.Root

import scala.tools.nsc.interpreter.IR

class RootController[F[_]: Monad](intp: Interpretator[F]) extends Root.Controller[F] {

  override def version: F[Root.Version] = Root.Version("0.1").pure

  override def interpret(body: Vector[String]): F[Result] = {
    val maybeResult = for {
      nel <- NonEmptyVector.fromVector(body).toOptionT[F]
      res <- OptionT.liftF(intp.interpret(NonEmptyList.fromReducible(nel)))
    } yield res
    maybeResult.getOrElse(Result(IR.Error, "", None))
  }
}