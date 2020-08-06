package echo.spec

class FileContentsSpec extends EchoSpec {
  "indexOf" in {
    FileContents(
      """fred
        |fred
        |fred""".stripMargin
    ).indexOf("fred", SourceRegion(3, 3)) <=> 10
  }
}
