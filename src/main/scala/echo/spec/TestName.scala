package echo.spec

import java.io.File
import reify.Reify
import scalaz.deriving

@deriving(Reify)
case class TestName(value: String) {
  def jsonFile(scalaSrc: ScalaSrc): File = file(scalaSrc, "json")
  def reifiedFile(scalaSrc: ScalaSrc): File = file(scalaSrc, "reified")

  def file(scalaSrc: ScalaSrc, extension: String): File =
    new File(scalaSrc.dir(extension), s"${value.replace("/", "").replace(" ", "_")}.$extension")
}
