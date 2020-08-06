package echo.spec

import reify.Reify
import scalaz.deriving


@deriving(Reify)
case class LineNumber(value: Int) {
  def +(offset: Int): LineNumber = LineNumber(value + offset)
}

object LineNumber {
  val values: Stream[Int] = Stream.iterate(1)(_ + 1)
}
