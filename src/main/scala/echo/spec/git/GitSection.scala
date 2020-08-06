package echo.spec.git

import echo.spec.SourceRegion
import reify.Reify
import scalaz.deriving


@deriving(Reify)
case class GitSection(region: SourceRegion, lines: DiffLines) {
  def print: Unit = lines.print
}
