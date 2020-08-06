package echo.spec.git

import java.io.{PrintStream, PrintWriter}
import org.scalatest.exceptions.TestFailedException


class DiffException extends TestFailedException(0) {
  override def printStackTrace(): Unit               = {}
  override def printStackTrace(s: PrintStream): Unit = {}
  override def printStackTrace(s: PrintWriter): Unit = {}
}
