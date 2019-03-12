package ru.tinkoff.codefest.http

import cats.Monad
import cats.data.{NonEmptyList, NonEmptyVector, OptionT}
import cats.syntax.applicative._
import cats.syntax.option._

import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.http.api.Root

class RootController[F[_]: Monad](intp: Interpretator[F]) extends Root.Controller[F] {

  override def version: F[Root.Version] = Root.Version("0.1").pure

  override def interpret(body: Vector[String]): F[Vector[String]] = {
    val maybeVector = for {
      nel <- NonEmptyVector.fromVector(body.reverse).toOptionT[F]
      res <- OptionT.liftF(intp.interpret(NonEmptyList.fromReducible(nel)))
    } yield Vector(res.output)
    maybeVector.getOrElse(Vector.empty[String])
  }
}
