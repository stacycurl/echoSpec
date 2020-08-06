package echo.spec

import reify.Formatter


case class Replacements(value: Map[ScalaSrc, List[Replacement]]) {
  def +(replacement: (ScalaSrc, Replacement)): Replacements =
    add(replacement._1, replacement._2)

  def add(src: ScalaSrc, replacement: Replacement): Replacements = Replacements(value + (src -> (value.get(src) match {
    case None                     => List(replacement)
    case Some(replacementsForSrc) => replacementsForSrc :+ replacement
  })))

  def applyTo(formatter: Formatter): Unit = value.foreach {
    case (scalaSrc, replacements) => {
      val resultingContents = replacements.sorted.foldLeft(scalaSrc.readContents()) {
        case (contents, replacement) => replacement.applyTo(contents, formatter)
      }

      scalaSrc.writeContents(resultingContents)
    }
  }
}

object Replacements {
  val empty: Replacements = Replacements(Map.empty)
}