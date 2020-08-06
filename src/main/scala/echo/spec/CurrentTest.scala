package echo.spec

import scala.language.implicitConversions

import java.io.File
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import echo.spec.internal.prelude._
import reify.internal.util.Extractor
import reify.internal.util.Extractor.&&
import reify.internal.util.classTag.simpleClassName
import reify.{Formatter, Reified, Reify}
import scalaz.deriving

import scala.meta.Term.ApplyInfix
import scala.meta.{Source, Term, Tree, XtensionParseInputLike}
import scala.reflect.ClassTag


@deriving(Reify)
case class CurrentTest(scalaSrcFile: ScalaSrc, className: ClassName, testName: TestName, lineNumber: LineNumber) {
  def jsonFile: File = testName.jsonFile(scalaSrcFile)
  def reifiedFile: File = testName.reifiedFile(scalaSrcFile)

  def findOrReplace(searchFor: Reified, replaceWith: Reified): Option[SourceRegion] = {
    getReplacement(searchFor, replaceWith).map(replacement => {
      if (searchFor != replaceWith) {
        val sourceText: FileContents = scalaSrcFile.readContents()
        val updatedText = replacement.applyTo(sourceText, Formatter.Indented(margin = 100, escape = false))

        scalaSrcFile.writeContents(updatedText)
      }

      replacement.sourceRegion
    })
  }

  def getReplacement(searchFor: Reified, replaceWith: Reified): Option[Replacement] = {
    val searchForS = Formatter.Unindented.format(searchFor.escape.tokenize)
    val searchForTerm: Term = searchForS.parse[Term].get
    val sourceText: FileContents = scalaSrcFile.readContents()

    val replacementRef: AtomicReference[Option[Replacement]] = new AtomicReference[Option[Replacement]](None)

    val InfixOp : Extractor[Tree, (Term, Term)] = Extractor.from[Tree].collect {
      case ApplyInfix(lhs, Term.Name("<=>"), Nil, List(rhs)) if rhs.structure == searchForTerm.structure => (lhs, rhs)
    }

    sourceText.value.parse[Source].get.traverse {
      case InfixOp((lhs, rhs)) && SourceRegion.FromTree(sourceRegion) if sourceRegion.contains(lineNumber) => {
        replacementRef.set(Some(Replacement.fromTerms(lhs, rhs, replaceWith, sourceRegion)))
      }
    }

    replacementRef.get()
  }

  def trace[A](name: String, content: A): Unit = {
    val index = traceIndex.getAndIncrement()

    val traceDir = testName.file(scalaSrcFile, "trace")
    traceDir.mkdirs()

    val traceFile = new File(traceDir, s"${index.toString.prefixPadTo(4, '0')}-$name")

    traceFile.writeString(content.toString)
  }

  implicit class ExtractorQSyntax[A, B](val self: Extractor[A, B]) {
    def trace(name: String)(implicit A: ClassTag[A], B: ClassTag[B]): Extractor[A, B] = {
      trace(name, a => optB =>
        s"""${simpleClassName[A]}: $a
           |
           |opt${simpleClassName[B]}: $optB
           |""".stripMargin)
    }

    def trace(name: String, tap: A => Option[B] => String): Extractor[A, B] = {
      self.tap(around = a => optB => CurrentTest.this.trace(name, tap(a)(optB)))
    }
  }

  private val traceIndex = new AtomicInteger(1)
}
