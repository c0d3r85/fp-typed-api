package ru.tinkoff.codefest.http

import cats.Monad
import cats.syntax.applicative._
import ru.tinkoff.codefest.http.api.Root

class RootController[F[_]: Monad] extends Root.Controller[F] {

  override def version: F[Root.Version] = Root.Version("0.1").pure

}
