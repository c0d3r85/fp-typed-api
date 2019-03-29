package ru.tinkoff.codefest.http

import scala.concurrent.Future

import akka.actor.ActorSystem
import cats.arrow.FunctionK
import cats.effect.{ExitCode, IO, IOApp}
import cats.~>

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    System.setSecurityManager(new BotSecurityManager)

    implicit val actorSystem: ActorSystem = ActorSystem("codefest")

    implicit val nt: IO ~> Future = {
      def unsafeToFuture[A](io: IO[A]): Future[A] = io.unsafeToFuture()
      FunctionK.lift[IO, Future](unsafeToFuture)
    }

    implicit val nt2: Future ~> IO = {
      def toIO[A](f: Future[A]): IO[A] = IO.fromFuture(IO(f))
      FunctionK.lift[Future, IO](toIO)
    }

    implicit val configModule: ConfigModule[IO] = new ConfigModuleImpl[IO]

    implicit val server: Server[IO] = new Server[IO]

    for {
      server <- IO.fromFuture(Server[IO].run(nt2))
      _ <- IO { println(server) }
      _ <- IO.never
    } yield ExitCode.Success
  }

}
