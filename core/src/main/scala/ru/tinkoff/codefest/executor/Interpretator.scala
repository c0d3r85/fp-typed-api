package ru.tinkoff.codefest.executor

import java.io.{ByteArrayOutputStream, FileInputStream, PrintWriter}
import java.lang.System.{lineSeparator => EOL}
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{IMain, IR}

import cats.data.NonEmptyList
import cats.effect._
import cats.syntax.option._

import ru.tinkoff.codefest.executor.Interpretator.{IRState, Result}

class Interpretator[F[_]: Sync] {
  private val F: Sync[F] = implicitly[Sync[F]]

  def interpret(code: NonEmptyList[String]): F[Result] = {
    val settings = new Settings
    settings.processArgumentString("-deprecation -feature")
    settings.usejavacp.value = true
    //settings.usemanifestcp.value = true использовать если cp, определен в манифесте, например для LauncherJarPlugin

    val foldFunction: IMain => (IRState, String) => IRState = intp => {
      case (IRState(IR.Error, _), _) => IRState(IR.Error)
      case (state, line) =>
        val code =
          if (state.status == IR.Incomplete)
            state.incompleteBuffer.fold(line)(b => s"$b\n$line")
          else line
        intp.interpret(code) match {
          case IR.Success    => IRState(IR.Success)
          case IR.Error      => IRState(IR.Error)
          case IR.Incomplete => IRState(IR.Incomplete, code.some)
        }
    }
    val res = for {
      out <- Resource.fromAutoCloseable(F.delay(new ByteArrayOutputStream))
      in <- Resource.fromAutoCloseable(F.delay(new FileInputStream("/dev/null")))
      writer <- Resource.fromAutoCloseable(F.delay(new PrintWriter(out)))
    } yield {
      val intp = new IMain(settings, writer)
      val state = Console.withIn(in) {
        Console.withOut(out) {
          val current = code.tail.flatMap(_.split(EOL)).foldLeft(IRState())(foldFunction(intp))
          out.reset()
          code.head.split(EOL).foldLeft(current)(foldFunction(intp))
        }
      }
      intp.close()
      out.flush()
      Result(state.status, out.toString)
    }
    res.use(F.pure)
  }
}

object Interpretator {
  final case class Result(status: IR.Result, output: String)
  final case class IRState(status: IR.Result = IR.Success, incompleteBuffer: Option[String] = None)
  def apply[F[_]: Interpretator]: Interpretator[F] = implicitly[Interpretator[F]]
}
