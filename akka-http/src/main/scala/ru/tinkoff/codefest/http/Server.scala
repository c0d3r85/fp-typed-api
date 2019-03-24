package ru.tinkoff.codefest.http

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.event.Logging.{InfoLevel, LogLevel}
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import cats.data.NonEmptyList
import cats.effect.{Async, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import com.bot4s.telegram.methods.SetWebhook
import com.bot4s.telegram.models.Update
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import ru.tinkoff.codefest.executor.{Interpretator, LocalInterpretator, RemoteInterpretator}
import ru.tinkoff.codefest.http.api.{Root, SwaggerModule, Telegram}
import ru.tinkoff.codefest.http.telegram.{TelegramController, TelegramModule}
import ru.tinkoff.codefest.storage.Storage
import ru.tinkoff.codefest.storage.postgresql.PostgreSQLStorage
import ru.tinkoff.codefest.telegram.{RequestHandler, ScalaReplBot, TelegramBot}

class Server[F[_]: ConfigModule: Sync: Async: Monad](
    implicit actorSystem: ActorSystem,
    t: F ~> Future
) {

  private def entityAsString(entity: HttpEntity): Future[String] =
    entity.dataBytes
      .map(_.decodeString(entity.contentType.charsetOption.getOrElse(HttpCharsets.`UTF-8`).value))
      .runWith(Sink.head)

  private def logRequestResult(level: LogLevel, route: Route) = {
    import actorSystem.dispatcher
    def myLoggingFunction(logger: LoggingAdapter)(req: HttpRequest)(res: Any): Unit = {
      val entry = res match {
        case Complete(resp) =>
          for {
            reqString <- entityAsString(req.entity)
            data <- entityAsString(resp.entity)
          } yield
            LogEntry(
              s"${req.method} ${req.uri} ${reqString}: ${resp.status} \n entity: $data",
              level
            )
        case other =>
          entityAsString(req.entity)
            .map(data => LogEntry(s"${req.method} ${req.uri} \n req entity: $data", level))
      }
      entry.map(_.logTo(logger))
    }
    DebuggingDirectives.logRequestResult(LoggingMagnet(log => myLoggingFunction(log)))(route)
  }

  def run(implicit nt: Future ~> F): F[Future[ServerBinding]] = {
    import actorSystem.dispatcher
    for {
      config <- ConfigModule[F].load
      handler = new RequestHandler[F](token = config.telegram.token)
      modules = NonEmptyList(rootModule, telegramModule(config.telegram)(handler, nt) :: Nil)
      routes = modules.map(_.route).toList.reduce(_ ~ _) ~ SwaggerModule.routes(rootModule.swagger)
      b <- Sync[F].delay(
        Http().bindAndHandle(logRequestResult(InfoLevel, routes), "0.0.0.0", config.web.port)
      )
      _ <- registerTelegramWebhook(config.telegram)(handler)
    } yield b

  }

  private implicit val materializer: Materializer = ActorMaterializer()

  private implicit val sttp: SttpBackend[F, Nothing] = AsyncHttpClientCatsBackend()

  private def rootModule: ApiModule[F] = {

    implicit val controller: Root.Controller[F] = new RootController[F](new RemoteInterpretator)

    new RootModule[F]
  }

  private def telegramModule(
      config: TelegramConfig
  )(implicit handler: RequestHandler[F], nt: Future ~> F): ApiModule[F] = {

    import actorSystem.dispatcher
    implicit val storage: Storage[F] = new PostgreSQLStorage

    implicit val interpretator: Interpretator[F] = new RemoteInterpretator[F]

    implicit val telegramBot: TelegramBot[F] = new ScalaReplBot[F]

    implicit val controller: Telegram.Controller[F] = new TelegramController[F](config.token)

    new TelegramModule[F]
  }

  private def registerTelegramWebhook(
      config: TelegramConfig
  )(implicit handler: RequestHandler[F]): F[Unit] =
    handler(SetWebhook(url = config.webhook)).void

}

object Server {

  final case class Config()

  def apply[F[_]: Server]: Server[F] = implicitly[Server[F]]

}
