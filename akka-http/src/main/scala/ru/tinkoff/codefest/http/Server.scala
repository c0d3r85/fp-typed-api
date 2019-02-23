package ru.tinkoff.codefest.http

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import cats.data.NonEmptyList
import cats.effect.{Async, Sync}
import cats.{Monad, ~>}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.bot4s.telegram.methods.SetWebhook
import com.bot4s.telegram.models.Update
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend

import ru.tinkoff.codefest.executor.Interpretator
import ru.tinkoff.codefest.{RequestHandler, TelegramBot}
import ru.tinkoff.codefest.http.api.{Root, Telegram}

class Server[F[_]: ConfigModule: Sync: Async: Monad](
    implicit actorSystem: ActorSystem,
    nt: F ~> Future
) {

  def run(): F[Future[ServerBinding]] = {
    import actorSystem.dispatcher
    for {
      config <- ConfigModule[F].load
      handler = new RequestHandler[F](token = config.telegram.token)
      modules = NonEmptyList(rootModule, telegramModule(config.telegram, handler) :: Nil)
      routes = modules.tail.foldLeft(modules.head.route) { (acc, module) =>
        acc ~ module.route
      }
      b <- Sync[F].delay(Http().bindAndHandle(routes, "0.0.0.0", config.web.port))
      _ <- registerTelegramWebhook(config.telegram, handler)
    } yield b

  }

  private implicit val materializer: Materializer = ActorMaterializer()

  private implicit val sttp: SttpBackend[F, Nothing] = AsyncHttpClientCatsBackend()

  private def rootModule: ApiModule[F] = {

    implicit val controller: Root.Controller[F] =
      new RootController[F](new Interpretator)

    new RootModule[F]
  }

  private def telegramModule(config: TelegramConfig, handler: RequestHandler[F]): ApiModule[F] = {

    implicit val telegramBot: TelegramBot[F] = new TelegramBot[F] {

      override def update(body: Update): F[Unit] = {
        println(body)
        Monad[F].unit
      }
    }

    implicit val controller: Telegram.Controller[F] = new TelegramController[F](config.token)

    new TelegramModule[F]
  }

  private def registerTelegramWebhook(config: TelegramConfig, handler: RequestHandler[F]): F[Unit] =
    handler(SetWebhook(url = config.webhook)).void

}

object Server {

  final case class Config()

  def apply[F[_]: Server]: Server[F] = implicitly[Server[F]]

}
