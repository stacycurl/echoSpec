package echo.spec.git

import echo.spec.{EchoSpec, SourceRegion}


class GitDiffSpec extends EchoSpec {
  "create" in {
    GitDiff.create(lines, None) <=> GitDiff(
      List(
        DiffLine("""diff --git a/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala b/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala"""),
        DiffLine("""index b2de22b..59cbe7c 100644"""),
        DiffLine("""--- a/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala"""),
        DiffLine("""+++ b/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala""")
      ),
      List(
        GitSection(
          SourceRegion(4, 4),
          DiffLines(
            List(
              DiffLine("""@@ -4 +4 @@"""),
              DiffLine("""-import echo.spec.{EchoSpec, Replacement}"""),
              DiffLine("""+import echo.spec.{EchoSpec, Replacement, SourceRegion}""")
            )
          )
        ),
        echo.spec.git.GitSection(
          SourceRegion(11, 11),
          DiffLines(
            List(
              DiffLine("""@@ -11 +11,2 @@ class ReplacementSpec extends EchoSpec {"""),
              DiffLine("""-      Token.infix(Token.primitive("actual"), " <=> ", Token.primitive("actual"))"""),
              DiffLine("""+      Token.infix(Token.primitive("actual"), " <=> ", Token.primitive("actual")),"""),
              DiffLine("""+      SourceRegion.unknown""")
            )
          )
        ),
        echo.spec.git.GitSection(
          SourceRegion(21, 21),
          DiffLines(
            List(
              DiffLine("""@@ -21 +22,2 @@ class ReplacementSpec extends EchoSpec {"""),
              DiffLine("""-      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting))"""),
              DiffLine("""+      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting)),"""),
              DiffLine("""+      SourceRegion.unknown""")
            )
          )
        ),
        echo.spec.git.GitSection(
          SourceRegion(31, 31),
          DiffLines(
            List(
              DiffLine("""@@ -31 +33,2 @@ class ReplacementSpec extends EchoSpec {"""),
              DiffLine("""-      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting))"""),
              DiffLine("""+      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting)),"""),
              DiffLine("""+      SourceRegion.unknown""")
            )
          )
        )
      )
    )
  }

  private val lines: List[String] =
    """diff --git a/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala b/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala
      |index b2de22b..59cbe7c 100644
      |--- a/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala
      |+++ b/echoSpec/src/test/scala/echo/spec/ReplacementSpec.scala
      |@@ -4 +4 @@
      |-import echo.spec.{EchoSpec, Replacement}
      |+import echo.spec.{EchoSpec, Replacement, SourceRegion}
      |@@ -11 +11,2 @@ class ReplacementSpec extends EchoSpec {
      |-      Token.infix(Token.primitive("actual"), " <=> ", Token.primitive("actual"))
      |+      Token.infix(Token.primitive("actual"), " <=> ", Token.primitive("actual")),
      |+      SourceRegion.unknown
      |@@ -21 +22,2 @@ class ReplacementSpec extends EchoSpec {
      |-      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting)))
      |+      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting))),
      |+      SourceRegion.unknown
      |@@ -31 +33,2 @@ class ReplacementSpec extends EchoSpec {
      |-      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting)))
      |+      Token.infix(Token.primitive("actual"), " <=> ", tokenize(something(interesting))),
      |+      SourceRegion.unknown""".stripMargin.split("\n").toList
}
