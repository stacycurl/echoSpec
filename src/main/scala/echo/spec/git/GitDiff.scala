package echo.spec.git

import echo.spec.internal.prelude._
import echo.spec.{ClassName, SourceRegion}
import reify.Reify
import scalaz.deriving



@deriving(Reify)
case class GitDiff(preamble: List[DiffLine], sections: List[GitSection]) {
  def nonEmpty: Boolean = !isEmpty
  def isEmpty: Boolean = sections.isEmpty

  def filterRegion(region: Option[SourceRegion]): GitDiff =
    region.fold(this)(filterRegion)

  def filterRegion(region: SourceRegion): GitDiff =
    GitDiff(preamble, sections.filter(section => region.contains(section.region)))

  def printAndThrow(cause: String): Nothing = {
    sections.foreach(_.print)

    throw (new DiffException).mutate(
      stackTrace = _.map(_.copy(fileName = s"file:///$cause\\ "))
    )
  }

  def printAndThrowX(className: ClassName): Nothing = {
    sections.foreach(_.print)

    throw new Exception("There's a difference between the committed and generated code").mutate(
      stackTrace = _.dropWhile(_.getClassName != className.value)
    )
  }
}

object GitDiff {
  def create(lines: List[String], region: Option[SourceRegion]): GitDiff = {
    val diffLines = DiffLines.create(lines).value
    val preamble = diffLines.takeWhile(_.sourceRegion.isEmpty)
    val rest: List[DiffLine] = diffLines.dropWhile(_.sourceRegion.isEmpty)

    val sections: List[GitSection] = {
      val batched = rest.batchBy(_.sourceRegion)
      val grouped = batched.grouped(2)

      grouped.toList.map {
        case List(List(regionLine), nonRegionLines) => echo.spec.git.GitSection(
          regionLine.sourceRegion.getOrElse(SourceRegion.unknown),
          DiffLines(regionLine :: nonRegionLines)
        )
        case other: List[List[DiffLine]] => echo.spec.git.GitSection(SourceRegion.unknown, DiffLines(other.flatten))
      }
    }

    val diff     = GitDiff(preamble, sections)
    val filtered = diff.filterRegion(region)

    filtered
  }
}