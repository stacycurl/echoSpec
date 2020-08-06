package echo.spec.cmd


case class Cmd(cmd: String, args: List[String]) {
  def exec: CmdResult = Cmd.exec(cmd, args: _*)
}

object Cmd {
  def apply(cmd: String, args: String*): Cmd =
    Cmd(cmd, args.toList)

  def exec(cmd: String, args: String*): CmdResult = {
    import scala.sys.process._

    val lines = List.newBuilder[String]
    val logger = ProcessLogger(lines += _)

    val exitValue: Int = Process(cmd, args.toList).run(logger).exitValue()

    CmdResult(exitValue, lines.result())
  }
}
