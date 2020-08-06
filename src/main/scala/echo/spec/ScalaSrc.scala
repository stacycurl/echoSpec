package echo.spec

import scala.language.implicitConversions

import java.io.File
import echo.spec.internal.prelude._
import reify.Reify
import reify.internal.prelude._
import scalaz.deriving

import scala.collection.{mutable => M}

@deriving(Reify)
case class ScalaSrc(value: String) {
  def dir(extension: String): File =
    new File(value.stripSuffix(".scala").suffixWith(s".$extension")).tap(_.mkdirs())

  val file: File = new File(value)
  def readContents(): FileContents = FileContents(readString())
  def writeContents(contents: FileContents): Unit = writeString(contents.value)

  def readString(): String = synchronized {
    contents = Option(contents).getOrElse({
      // println("reading")
      file.readString()
    })
    contents
  }

  def writeString(contents: String): Unit = synchronized {
    // println("writing")
    file.writeString(contents)
    this.contents = contents
  }

  private var contents: String = _
}

object ScalaSrc {
  def create(value: String): ScalaSrc = synchronized {
    cache.getOrElseUpdate(value, ScalaSrc(value))
  }

  private val cache: M.Map[String, ScalaSrc] = M.Map[String, ScalaSrc]()
}