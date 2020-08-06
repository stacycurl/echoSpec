package echo.spec

import reify.Token.Primitive
import reify.{Formatter, Reify, Token}
import scalaz.deriving


class ReplacementSpec extends EchoSpec {
  "simple" reifyEquals {
    Replacement(
      Primitive("actual"), Primitive("expected"), Primitive("actual"), SourceRegion(1, 1)
    ).applyTo(
      FileContents("""actual <=> expected""".stripMargin),
      Formatter.Indented(margin = 50, escape = false)
    )
  }

  "requiring indent" reifyEquals {
    Replacement(
      Primitive("actual"), Primitive("expected"), tokenize(solarSystem), SourceRegion(1, 1)
    ).applyTo(
      FileContents("""actual <=> expected""".stripMargin),
      Formatter.Indented(margin = 50, escape = false)
    )
  }

  "requiring indent and nesting" reifyEquals {
    Replacement(
      Primitive("actual"), Primitive("expected"), tokenize(solarSystem), SourceRegion(2, 2)
    ).applyTo(
      FileContents("""something {
        |  actual <=> expected
        |}""".stripMargin),
      Formatter.Indented(margin = 50, escape = false)
    )
  }

  private def tokenize[A: Reify](a: A): Token = Reify.of[A].reify(a).tokenize

  private val solarSystem = SolarSystem(
    Jupiter(
      Ganymede(2634),
      Calisto(2410),
      Io(1822),
      Europa(1561)
    )
  )
}

@deriving(Reify)
case class SolarSystem(jupiter: Jupiter)

@deriving(Reify)
case class Jupiter(ganymede: Ganymede, calisto: Calisto, io: Io, europa: Europa)

@deriving(Reify)
case class Io(radius: Int)

@deriving(Reify)
case class Ganymede(radius: Int)

@deriving(Reify)
case class Europa(radius: Int)

@deriving(Reify)
case class Calisto(radius: Int)