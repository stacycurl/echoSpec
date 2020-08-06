package echo.spec.git

import echo.spec.{EchoSpec, SourceRegion}


class DiffLineSpec extends EchoSpec {
  "sourceRegion" - {
    "no region" in {
      DiffLine("diff --git a/blah b/yada").sourceRegion <=> None
    }

    "with region" in {
      DiffLine("@@ -80,7 +80,7 @@ who cares").sourceRegion <=> Some(SourceRegion(80, 87))
    }

    "another with region" in {
      DiffLine("@@ -4 +4 @@ whatever").sourceRegion <=> Some(SourceRegion(4, 4))
    }
  }
}
