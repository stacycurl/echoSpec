package echo.spec

import reify.Reify
import reify.internal.util.Extractor
import scalaz.deriving

import scala.meta.Tree


@deriving(Reify)
case class SourceRegion(from: Int, to: Int) {
  def toTuple: (Int, Int) = (from, to)

  def +(offset: Int): SourceRegion = SourceRegion(from + offset, to + offset)

  def contains(region: SourceRegion): Boolean = contains(region.from) && contains(region.to)
  def contains(line: LineNumber): Boolean     = contains(line.value)
  def contains(line: Int): Boolean            = line >= from && line <= to

  def isBefore(line: LineNumber): Boolean = isBefore(line.value)
  def isBefore(line: Int):        Boolean = from < line

  def size: Int = to - from
}

object SourceRegion {
  implicit val sourceRegionOrdering =
    Ordering.Tuple2[Int, Int].on[SourceRegion](_.toTuple)

  val unknown: SourceRegion = SourceRegion(0, Int.MaxValue)

  val FromTree: Extractor[Tree, SourceRegion] =
    Extractor.from[Tree].property(tree => SourceRegion(tree.pos.startLine + 1, tree.pos.endLine + 1))
}