package ru.tinkoff.codefest.executor
import cats.data.NonEmptyList

trait Interpretator[F[_]] {
  def interpret(code: NonEmptyList[String]): F[Result]
}

object Interpretator {
  def apply[F[_]: Interpretator]: Interpretator[F] = implicitly[Interpretator[F]]
}
