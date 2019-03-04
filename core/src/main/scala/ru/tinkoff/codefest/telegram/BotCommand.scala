package ru.tinkoff.codefest.telegram

import cats.syntax.option._
import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait BotCommand extends EnumEntry with Lowercase

object BotCommand extends Enum[BotCommand] {

  case object Reset extends BotCommand
  case object State extends BotCommand

  val values = findValues

  private val pattern = s"/(${values.map(_.entryName).mkString("|")})".r

  def unapply(arg: String): Option[BotCommand] = arg match {
    case pattern(cmd) => BotCommand.withName(cmd).some
    case _            => None
  }
}
