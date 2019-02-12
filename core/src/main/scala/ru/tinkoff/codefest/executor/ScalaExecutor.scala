package ru.tinkoff.codefest.executor

import scala.language.higherKinds
import scala.reflect.runtime.{universe => ru}
import scala.tools.reflect.{ToolBox, ToolBoxError}
import scala.util.Try

import cats.Id

trait ScalaExecutor[F[_]] {
  def execute(code: Vector[String]): F[Either[Vector[String], Any]]
}

class ReflectScalaExecutor extends ScalaExecutor[Id] {
  import ReflectScalaExecutor._

  private val prelude = "import Predef.{print => _, println => _, printf => _, _}"

  override def execute(code: Vector[String]): Id[Either[Vector[String], Any]] = {
    val fullCodeSnippet =
      s"""
        |$prelude
        |${code.mkString("\n")}
      """.stripMargin

    Try(toolBox.eval(toolBox.parse(fullCodeSnippet))).toEither.left.map {
      case ToolBoxError(message, _) =>
        message.split('\n').filter(_.nonEmpty).toVector
    }
  }
}
object ReflectScalaExecutor {
  // https://groups.google.com/forum/#!topic/scala-user/R0Ff2F6Jkrw
  private val toolBox = ru.runtimeMirror(getClass.getClassLoader).mkToolBox()
}
