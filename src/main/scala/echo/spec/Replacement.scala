package echo.spec

import java.io.File
import echo.spec.git.{DiffLines, Git}
import echo.spec.internal.prelude._
import reify.Token.Primitive
import reify.{Formatter, Reified, Token}

import scala.meta.Term


case class Replacement(lhsT: Token, fromT: Token, toT: Token, sourceRegion: SourceRegion) {
  def diff(original: FileContents, formatter: Formatter): Option[DiffLines] = {
    val fromS = formatter.format(from)

    val fromIndex = original.indexOf(fromS)

    val toS = if (fromIndex == -1) formatter.format(to) else {
      getNestedToS(original, formatter, fromIndex)
    }

    Git.diff(fromS, toS)
  }

  def applyTo(file: File, formatter: Formatter): Unit = {
    val result = applyTo(FileContents(file.readString()), formatter)

    result.writeTo(file)
  }

  def applyTo(content: FileContents, formatter: Formatter): FileContents = {
    val fromS = formatter.format(from)

    val fromIndex = content.indexOf(fromS, sourceRegion)

    if (fromIndex != -1) {
      val nestedToS = getNestedToS(content, formatter, fromIndex)

      content.splice(fromIndex, fromS, nestedToS)
    } else {
      content
    }

//    content.replace(fromS, toS)
  }

  private def getNestedToS(content: FileContents, formatter: Formatter, fromIndex: Int): String = {
    val toS = formatter.format(to)

    val toSSplit = toS.split("\n")

    val infixOpIndex = toSSplit.indexWhere(_.contains(" <=> "))

    if (infixOpIndex == -1) toS else {
      val indent = content.getIndent(fromIndex)

      toSSplit.zipWithIndex.map {
        case (line, index) if index > infixOpIndex => s"$indent$line"
        case (line, _) => line
      }.mkString("\n")
    }

  }

  private def from: Token = lhsT <=> fromT
  private def to:   Token = lhsT <=> toT
}

object Replacement {
  implicit val replacementOrdering: Ordering[Replacement] =
    SourceRegion.sourceRegionOrdering.on[Replacement](_.sourceRegion).reverse

  def fromTerms(lhs: Term, from: Term, to: Reified, sourceRegion: SourceRegion): Replacement = {
    val lhsT  = Primitive(lhs.toString())
    val fromT = Primitive(from.toString())

    Replacement(lhsT, fromT, to.tokenize, sourceRegion)
  }

  private def withoutIndent(value: String): String = {
    val result: List[String] = value.split("\n").toList match {
      case notIndented :: indented => notIndented :: withoutIndent(indented)
      case other                   => other
    }

    result.mkString("\n")
  }

  private def withoutIndent(list: List[String]): List[String] = {
    val minIndent         = list.map(_.takeWhile(_ == ' ').length).min
    val withoutIndentList = list.map(_.substring(minIndent))

    withoutIndentList
  }
}