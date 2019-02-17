package ru.tinkoff.codefest.http

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.{Monad, ~>}
import cats.syntax.flatMap._
import cats.syntax.functor._

import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.http.api.Root

class Server[F[_]: ConfigModule: Sync: Monad](implicit actorSystem: ActorSystem,
                                nt: F ~> Future) {

  def run(config: Server.Config): F[Future[ServerBinding]] = {
    import actorSystem.dispatcher

    val routes: Route = modules.tail.foldLeft(modules.head.route) {
      case (acc, module) => acc ~ module.route
    }

    def binding(config: Config) =
      Http().bindAndHandle(routes, "0.0.0.0", config.web.port)

    for {
      config <- ConfigModule[F].load
      b <- Sync[F].delay(binding(config))
    } yield b

  }

  private implicit val materializer: Materializer = ActorMaterializer()

  private val rootModule: RootModule[F] = {

    implicit val controller: Root.Controller[F] =
      new RootController[F](new Interpretator)

    new RootModule[F]
  }

  private val modules: NonEmptyList[ApiModule[F]] =
    NonEmptyList(rootModule, Nil)

}

object Server {

  final case class Config()

  def apply[F[_]: Server]: Server[F] = implicitly[Server[F]]

}
