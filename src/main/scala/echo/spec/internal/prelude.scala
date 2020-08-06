package echo.spec.internal

import java.io.{File, FileOutputStream, RandomAccessFile}
import reify.internal.prelude._

import scala.collection.immutable.{List, Nil}
import scala.collection.{mutable => M}
import scala.io.Codec


object prelude {
  implicit class URLMatcher(val sc: StringContext) extends AnyVal {
      def m = sc.parts.mkString("(.+)")
        .replaceAllLiterally("?", "\\?")
        .r
    }

  implicit class ESStringSyntax(val self: String) extends AnyVal {
    def indexesOf(searchFor: String): List[Int] = {
      val length = searchFor.length

      @scala.annotation.tailrec
      def loop(acc: List[Int], fromIndex: Int): List[Int] = {
        val indexOf = self.indexOf(searchFor, fromIndex)

        if (indexOf != -1) loop(indexOf :: acc, indexOf + length) else acc.reverse
      }

      loop(Nil, 0)
    }

    def prefixPadTo(len: Int, elem: Char): String = (elem.toString * (len - self.length)) + self

    def suffixWith(suffix: String): String = if (self.endsWith(suffix))   self else self + suffix
  }

  implicit class ESListSyntax[A](val self: List[A]) extends AnyVal {
    def batchBy[B](f: A ⇒ B): List[List[A]] = self match {
      case Nil          => Nil
      case head :: tail => {
        val (_, lastBatch, allBatches) = tail.foldLeft((f(head), M.ListBuffer(head), M.ListBuffer[List[A]]())) {
          case ((currentKey, batch, batches), a) ⇒ {
            val key = f(a)

            if (key == currentKey) {
              (key, batch += a, batches)
            } else {
              (key, M.ListBuffer(a), batches += batch.toList)
            }
          }
        }

        (allBatches += lastBatch.toList).toList
      }
    }
  }

  implicit class ESThrowableSyntax(val self: Throwable) extends AnyVal {
    def mutate(
      stackTrace: Array[StackTraceElement] => Array[StackTraceElement] = identity
    ): Throwable = {
      self.setStackTrace(stackTrace(self.getStackTrace))
      self
    }
  }

  implicit class ESStackTraceElementSyntax(val self: StackTraceElement) extends AnyVal {
    def modify(
      className: String => String = identity,
      methodName: String => String = identity,
      fileName: String => String = identity,
      lineNumber: Int => Int = identity
    ): StackTraceElement = copy(
      className = className(self.getClassName),
      methodName = methodName(self.getMethodName),
      fileName = fileName(self.getFileName),
      lineNumber = lineNumber(self.getLineNumber)
    )

    def copy(
      className: String = self.getClassName,
      methodName: String = self.getMethodName,
      fileName: String = self.getFileName,
      lineNumber: Int = self.getLineNumber
    ): StackTraceElement =
      new StackTraceElement(className, methodName, fileName, lineNumber)
  }

  implicit class ESFileSyntax(val self: File) extends AnyVal {
    def readString()(implicit codec: Codec): String = new String(readBytes(), codec.charSet)

    private def readBytes(): Array[Byte] = {
      val raf = new RandomAccessFile(self, "r")
      val bytes = new Array[Byte](raf.length().toInt)
      raf.read(bytes)
      raf.close()
      bytes
    }

    def writeString(contents: String): File = {
      new FileOutputStream(self).tap(_.write(contents.getBytes()), _.flush(), _.close())
      self
    }
  }
}
