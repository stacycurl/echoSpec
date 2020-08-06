package echo.spec

import scala.language.implicitConversions

import java.io.File
import java.util.concurrent.atomic.AtomicReference
import echo.spec.git.Git
import echo.spec.internal.prelude._
import org.scalatest
import org.scalatest.{Args, BeforeAndAfterAll, FreeSpec, Matchers}
import reify.Reified.RPrimitive
import reify.{Formatter, ReifiedFields, Reify}
import sourcecode.Line


trait EchoSpec extends FreeSpec with Matchers with BeforeAndAfterAll { espec =>
  implicit class StringSpecSyntax(val self: String) {
    def reifyEquals[A: Reify](body: => A)(implicit line: Line, fileName: sourcecode.File): Unit = self in {
      espec.reifyEquals(body)
    }
  }

  implicit class InplaceReifyAssertSyntax[A](val self: A) {
    def <=>(rhs: => A)(implicit currentTest: CurrentTest, R: Reify[A]): Unit =
      inPlaceReifyEquals(self, rhs)
  }

  private def inPlaceReifyEquals[A](self: A, rhs: => A)(implicit currentTest: CurrentTest, R: Reify[A]): Unit = {
    val lhsReified = reifiedFields.reify(self)

    val rhsReified = try {
      reifiedFields.reify(rhs)
    } catch {
      case nie: NotImplementedError if nie.getMessage == "an implementation is missing" => RPrimitive("???")
    }

    currentTest.getReplacement(searchFor = rhsReified, replaceWith = lhsReified).foreach(replacement => {
      if (lhsReified != rhsReified) {
        replacements.updateAndGet(_ + (currentTest.scalaSrcFile -> replacement))

        replacement
          .diff(currentTest.scalaSrcFile.readContents(), Formatter.Indented(margin = 100, escape = false))
          .foreach(_.printAndThrowX(currentTest.className))
      } else {
        Git.diff(currentTest.scalaSrcFile, replacement.sourceRegion)
          .foreach(_.printAndThrowX(currentTest.className))
      }
    })
//
//
//      val diff = currentTest.findOrReplace(searchFor = rhsReified, replaceWith = lhsReified).flatMap(sourceRegion => {
//        currentTest.scalaSrcFile.diff(sourceRegion)
//      })
//
//      diff.foreach(_.printAndThrowX(currentTest.className))
  }

  private def reifyEquals[A: Reify](actual: A)(implicit line: Line, fileName: sourcecode.File): A = {
    val expectedReifiedFile: File = currentTest.reifiedFile

    val actualReified: String = Formatter.Indented(margin = 100, escape = false).format(Reify.of[A].reify(actual).tokenize)

    if (!expectedReifiedFile.exists()) {
      expectedReifiedFile.writeString(actualReified)
    } else {
      val expectedReified = expectedReifiedFile.readString()

      if (expectedReified != actualReified) {
        expectedReifiedFile.writeString(actualReified)
      }

      Git.diff(expectedReifiedFile, None).foreach(_.printAndThrow(expectedReifiedFile.getAbsolutePath))
    }

    actual
  }

  override protected def afterAll(): Unit = {
    super.afterAll()

    replacements.get().applyTo(Formatter.Indented(margin = 100, escape = false))
  }

  protected final implicit def currentTest(implicit line: Line, fileName: sourcecode.File): CurrentTest = CurrentTest(
    ScalaSrc.create(fileName.value),
    ClassName(getClass.getName),
    _currentTestName.get().getOrElse(sys.error("No test")),
    LineNumber(line.value)
  )

  override protected def runTest(testName: String, args: Args): scalatest.Status = {
    _currentTestName.set(Some(TestName(testName)))

    super.runTest(testName, args)
  }

  override def toString(): String = s"${getClass.getName} - ${_currentTestName.get().getOrElse("<unknown>")}"

  private lazy val reifiedFields = ReifiedFields.create(this).namesShorterThanDefinitions
  private val replacements = new AtomicReference[Replacements](Replacements.empty)
  private val _currentTestName: AtomicReference[Option[TestName]] = new AtomicReference[Option[TestName]](None)
}