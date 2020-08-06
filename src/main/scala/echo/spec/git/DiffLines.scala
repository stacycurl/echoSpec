package echo.spec.git

import echo.spec.internal.prelude._
import echo.spec.ClassName
import reify.Reify
import scalaz.deriving


@deriving(Reify)
case class DiffLines(value: List[DiffLine]) {
  def tail: DiffLines = DiffLines(value.tail)

  def printAndThrowX(className: ClassName): Nothing = {
    print

    throw new Exception("There's a difference between the committed and generated code").mutate(
      stackTrace = _.dropWhile(_.getClassName != className.value)
    )
  }

  def print: Unit = value.foreach(_.print)
}

object DiffLines {
  def create(lines: List[String]): DiffLines =
    DiffLines(lines.map(DiffLine))
}