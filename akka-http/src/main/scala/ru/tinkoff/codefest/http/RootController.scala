package ru.tinkoff.codefest.http

import cats.Monad
import cats.data.{NonEmptyList, NonEmptyVector, OptionT}
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.option._
import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.http.api.Root
import ru.tinkoff.codefest.http.api.Root.{IRResponse, Status}


class RootController[F[_]: Monad](intp: Interpretator[F]) extends Root.Controller[F] {

  override def version: F[Root.Version] = Root.Version("0.1").pure

  override def interpret(body: Vector[String]): F[IRResponse] = {
    val maybeResult = for {
      nel <- NonEmptyVector.fromVector(body.reverse).toOptionT[F]
      res <- OptionT.liftF(intp.interpret(NonEmptyList.fromReducible(nel)))
      status <- Status.withNameOption(res.status.getClass.getSimpleName.dropRight(1)).toOptionT
    } yield IRResponse(status, res.output, res.compiled)
    maybeResult.value.map(_.get)
  }
}
