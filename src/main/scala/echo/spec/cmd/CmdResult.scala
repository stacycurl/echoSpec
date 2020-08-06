package echo.spec.cmd

case class CmdResult(code: Int, lines: List[String]) {
  def nonEmptyLines: Option[List[String]] = if (lines.isEmpty) None else Some(lines)

  def print: Unit = {
    println(code)
    printLines
  }

  def printLines: Unit =
    lines.foreach(println)
}
