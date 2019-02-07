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

import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.http.api.Root

class Server[F[_]: Sync: Monad](implicit actorSystem: ActorSystem,
                                nt: F ~> Future) {

  def run(config: Server.Config): F[Future[ServerBinding]] = {
    import actorSystem.dispatcher

    val routes: Route = modules.tail.foldLeft(modules.head.route) {
      case (acc, module) => acc ~ module.route
    }

    def binding = Http().bindAndHandle(routes, "", 8080)

    Sync[F].delay(binding)

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
