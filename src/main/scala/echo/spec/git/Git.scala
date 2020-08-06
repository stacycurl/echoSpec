package echo.spec.git

import java.io.File
import echo.spec.cmd.{Cmd, CmdResult}
import echo.spec.internal.prelude._
import echo.spec.{ScalaSrc, SourceRegion}
import reify.internal.prelude._


object Git {
  def diff(from: String, to: String): Option[DiffLines] = {
    val fromF = File.createTempFile("txt", "diff-from-").tap(_.deleteOnExit()).writeString(from.suffixWith("\n"))
    val toF   = File.createTempFile("txt", "diff-to-").tap(_.deleteOnExit()).writeString(to.suffixWith("\n"))

    val execResult: CmdResult = Cmd(
      "git", "-c", "color.ui=never", "--no-pager", "diff", "--no-index", "-U0", "--",
//      "git", "-c", "color.ui=always", "--no-pager", "diff", "--word-diff=color", "--no-index", "-U0", "--",
      escapePath(fromF),
      escapePath(toF),
    ).exec

    for {
      lines        <- execResult.nonEmptyLines
      firstSection <- GitDiff.create(lines, None).sections.headOption
    } yield firstSection.lines.tail
  }

  def diff(src: ScalaSrc, region: SourceRegion): Option[GitDiff] =
    diff(src.file, Some(region))

  def diff(file: File, region: Option[SourceRegion]): Option[GitDiff] = {
    val execResult: CmdResult =
      Cmd("git", "-c", "color.ui=always", "--no-pager", "diff", "-U0", "--", escapePath(file)).exec

    //Cmd("git", "-c", "color.ui=always", "-c", "icdiff.options='--cols=220'", "--no-pager", "icdiff", "--", file.getPath.replace(" ", "\\ ")).exec

    execResult.nonEmptyLines.map(GitDiff.create(_, region)).filter(_.nonEmpty)
  }

  private def escapePath(file: File): String = file.getPath.replace(" ", "\\ ")
}
