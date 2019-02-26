package ru.tinkoff.codefest.executor

import java.io.{ByteArrayOutputStream, FileInputStream, PrintWriter}
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain

import cats.effect._

class Interpretator[F[_]: Sync] {
  private val F: Sync[F] = implicitly[Sync[F]]

  def interpret(code: Vector[String]): F[Vector[String]] = {
    val settings = new Settings
    settings.processArgumentString("-deprecation -feature")
    settings.usejavacp.value = true
    //settings.usemanifestcp.value = true использовать если cp, определен в манифесте, например для LauncherJarPlugin
    val res = for {
      out <- Resource.fromAutoCloseable(F.delay(new ByteArrayOutputStream))
      in <- Resource.fromAutoCloseable(F.delay {
        new FileInputStream("/dev/null")
      })
      writer <- Resource.fromAutoCloseable(F.delay(new PrintWriter(out)))
    } yield {
      val intp = new IMain(settings, writer)
      Console.withOut(out) {
        Console.withIn(in) {
          intp.interpret(code.dropRight(1).mkString("\n"))
          intp.interpret(code.last)
          out
        }
      }
    }
    res.use(os => F.pure(os.toString.split("\n").toVector))
  }
}

object Interpretator {

  def apply[F[_]: Interpretator]: Interpretator[F] = implicitly[Interpretator[F]]

}