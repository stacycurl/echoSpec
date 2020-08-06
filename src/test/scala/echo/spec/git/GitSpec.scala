package echo.spec.git

import echo.spec.EchoSpec


class GitSpec extends EchoSpec {
  "diff" in {
    Git.diff(
      """First line
        |Second line
        |Third line
        |""".stripMargin,
      """First line
        |2nd line
        |Third line !
        |""".stripMargin) <=> Some(
      DiffLines(
        List(
          DiffLine("-Second line"),
          DiffLine("-Third line"),
          DiffLine("+2nd line"),
          DiffLine("+Third line !")
        )
      )
    )
  }

  "getSurroundingBlock" - {
    "regular block" in {
      foo <=> "expected"
    }
  }

  val foo: String = "expected"
}