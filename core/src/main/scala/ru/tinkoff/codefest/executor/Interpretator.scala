package ru.tinkoff.codefest.executor

import java.io.{ByteArrayOutputStream, FileInputStream, PrintWriter}
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{IMain, IR}
import scala.util.Try
import scala.util.control.NonFatal

import cats.data.NonEmptyList
import cats.effect._
import cats.syntax.option._
import io.circe.generic.JsonCodec
import io.circe.{Decoder, Encoder}

import ru.tinkoff.codefest.executor.Interpretator.{IRState, Result}

class Interpretator[F[_]: Sync] {
  import Interpretator.EOL

  private val F: Sync[F] = implicitly[Sync[F]]

  val settings = new Settings
  val args = List(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:postfixOps",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Ypartial-unification",
    "-Xplugin:kind-projector"
  )
  settings.processArgumentString(args.mkString(" "))
  settings.usejavacp.value = true
  val in = new FileInputStream("/dev/null")
  val out = new ByteArrayOutputStream
  val writer = new PrintWriter(out)
  val intp = new IMain(settings, writer)

  def interpret(code: NonEmptyList[String]): F[Result] = {
    /*val settings = new Settings
    val args = List(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:postfixOps",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Ypartial-unification",
      "-Xplugin:kind-projector"
    )
    settings.processArgumentString(args.mkString(" "))
    settings.usejavacp.value = true*/
    //settings.usemanifestcp.value = true использовать если cp, определен в манифесте, например для LauncherJarPlugin

    val foldFunction: IMain => (IRState, String) => IRState = intp => {
      case (IRState(IR.Error, compiled, _), _) => IRState(IR.Error, compiled)
      case (state, line) =>
        val code =
          if (state.status == IR.Incomplete)
            state.incompleteBuffer.fold(line)(b => s"$b$EOL$line")
          else line
        val total = state.compiled.fold(code)(c => s"$c$EOL$code")
        Try(intp.interpret(code)).map {
          case IR.Success    => IRState(IR.Success, total.some)
          case IR.Incomplete => IRState(IR.Incomplete, state.compiled, code.some)
          case IR.Error      => IRState(IR.Error, state.compiled)
        } getOrElse IRState(IR.Error, state.compiled)
    }
    /* val res = for {
      out <- Resource.fromAutoCloseable(F.delay(new ByteArrayOutputStream))
      in <- Resource.fromAutoCloseable(F.delay(new FileInputStream("/dev/null")))
      writer <- Resource.fromAutoCloseable(F.delay(new PrintWriter(out)))
    } yield {
      //val intp = new IMain(settings, writer)
     */
    this.synchronized {
      try {
        val state = Console.withIn(in) {
          Console.withOut(out) {
            val current = code.tail.flatMap(_.split(EOL)).foldLeft(IRState())(foldFunction(intp))
            out.reset()
            code.head.split(EOL).foldLeft(current)(foldFunction(intp))
          }
        }
        out.flush()
        val compiled = List(state.compiled, state.incompleteBuffer).flatten.mkString(EOL)
        F.pure(Result(state.status, out.toString, compiled.some.filter(_.trim.nonEmpty)))
      } catch {
        case NonFatal(e) => F.raiseError(e)
      } finally {
        intp.reset()
        out.reset()
      }
    }
    // res.use(F.pure)
  }
}

object Interpretator {
  @JsonCodec
  final case class Result(status: IR.Result, output: String, compiled: Option[String])
  object Result {
    implicit val d: Decoder[IR.Result] = Decoder.decodeString.map {
      case "success"    => IR.Success
      case "error"      => IR.Error
      case "incomplete" => IR.Incomplete
    }
    implicit val e: Encoder[IR.Result] =
      Encoder.encodeString.contramap[IR.Result](_.getClass.getSimpleName.toLowerCase.dropRight(1))
  }
  final case class IRState(
      status: IR.Result = IR.Success,
      compiled: Option[String] = None,
      incompleteBuffer: Option[String] = None
  )
  private val EOL = "\n"
  def apply[F[_]: Interpretator]: Interpretator[F] = implicitly[Interpretator[F]]
}
