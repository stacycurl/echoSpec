package echo.spec.git

import scala.language.implicitConversions

import echo.spec.SourceRegion
import echo.spec.internal.prelude._
import reify.Reify
import reify.internal.util.Extractor.Int
import scalaz.deriving


@deriving(Reify)
case class DiffLine(line: String) {
  // TODO: This should be a one line extractor, figure out what when wrong with 'url'
  def sourceRegion: Option[SourceRegion] = for {
    range  <- PartialFunction.condOpt(line.split("@@").toList) {
      case List(_, range, _) => range
      case List(_, range) => range
    }
    region <- PartialFunction.condOpt(range.trim.split(" ").toList) {
      case List(m"-${Int(from)}", _)             => SourceRegion(from, from)
      case List(m"-${Int(from)},${Int(inc)}", _) => SourceRegion(from, from + inc)
    }
  } yield region

  def print: Unit = {
    if (!startsWith("---", "+++", "diff --git", "index")) {
      if (startsWith("+")) {
        Console.out.println(Console.GREEN + line + Console.RESET)
      } else if (startsWith("-")) {
        Console.out.println(Console.RED + line + Console.RESET)
      } else {
        Console.out.println(line)
      }
    }
  }

  private def startsWith(prefix: String, alternativePrefixes: String*): Boolean =
    (prefix :: alternativePrefixes.toList).exists(line.startsWith)
}
