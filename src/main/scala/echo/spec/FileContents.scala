package echo.spec

import scala.language.implicitConversions

import java.io.File
import echo.spec.internal.prelude._
import reify.Reify
import scalaz.deriving


@deriving(Reify)
case class FileContents(value: String) {
  private lazy val indexByLineNumber: Map[Int, (Int, Int)] = {
    val (_, _, result) = value.split("\n").toList.foldLeft((1, 0, Map.empty[Int, (Int, Int)])) {
      case ((lineNumber, fromIndex, acc), line) => {
        val toIndex = fromIndex + line.length

        (lineNumber + 1, toIndex + 1, acc + (lineNumber -> (fromIndex, toIndex)))
      }
    }

    result
  }

  def indexOf(searchFor: String): Int = value.indexOf(searchFor)

  def indexOf(searchFor: String, sourceRegion: SourceRegion): Int = {
    val indexes = value.indexesOf(searchFor)

    val matchingIndexes: Seq[Int] = for {
      index          <- indexes
      (fromIndex, _) <- indexByLineNumber.get(sourceRegion.from)
      (_, toIndex)   <- indexByLineNumber.get(sourceRegion.to)
      if (fromIndex <= index) && index <= toIndex
    } yield index

    matchingIndexes.headOption.getOrElse(-1)
  }

  def writeTo(file: File): File = file.writeString(value)

  def getIndent(index: Int): String = {
    val indent = value.substring(0, index).reverse.takeWhile(_ == ' ')

    indent
  }

  def splice(fromIndex: Int, fromS: String, toS: String): FileContents = {
    FileContents(value.substring(0, fromIndex) + toS + value.substring(fromIndex + fromS.length))
  }
}
