package ru.tinkoff.codefest.executor

import java.io.{ByteArrayOutputStream, FileInputStream, FileOutputStream, PrintWriter}
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain

import cats.data.NonEmptyList
import cats.effect._

class Interpretator[F[_]: Sync] {
  private val F: Sync[F] = implicitly[Sync[F]]

  def interpret(code: NonEmptyList[String]): F[String] = {
    val settings = new Settings
    settings.processArgumentString("-deprecation -feature")
    settings.usejavacp.value = true
    //settings.usemanifestcp.value = true использовать если cp, определен в манифесте, например для LauncherJarPlugin
    val res = for {
      out <- Resource.fromAutoCloseable(F.delay(new ByteArrayOutputStream))
      in <- Resource.fromAutoCloseable(F.delay(new FileInputStream("/dev/null")))
      outNull <- Resource.fromAutoCloseable(F.delay(new FileOutputStream("/dev/null")))
      writer <- Resource.fromAutoCloseable(F.delay(new PrintWriter(out)))
    } yield {
      val intp = new IMain(settings, writer)
      Console.withIn(in) {
        Console.withOut(outNull) {
          intp.interpret(code.tail.mkString("\n"))
          out.reset()
        }
        Console.withOut(out) {
          intp.interpret(code.head)
        }
      }
      intp.close()
      out
    }
    res.use(os => F.pure(os.toString))
  }
}

object Interpretator {

  def apply[F[_]: Interpretator]: Interpretator[F] = implicitly[Interpretator[F]]

}
