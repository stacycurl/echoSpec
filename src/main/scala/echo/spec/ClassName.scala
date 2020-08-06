package echo.spec

import scala.language.implicitConversions

import reify.Reify
import scalaz.deriving

@deriving(Reify)
case class ClassName(value: String)
