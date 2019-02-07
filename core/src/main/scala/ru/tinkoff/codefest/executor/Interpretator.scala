package ru.tinkoff.codefest.executor

import java.io.{ByteArrayOutputStream, FileInputStream, PrintWriter}
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain

import cats.effect._

class Interpretator[F[_]: Sync] {
  private val F: Sync[F] = implicitly[Sync[F]]

  def interpret(code: Vector[String]): F[Vector[String]] = {
    val settings = new Settings
    settings.processArgumentString(
      "-deprecation -feature -Xfatal-warnings -Xlint")
    //settings.usejavacp.value = true
    settings.usemanifestcp.value = true
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
          code.map(intp.interpret)
          out
        }
      }
    }
    res.use(os => F.pure(os.toString.split("\n").toVector))
  }
}