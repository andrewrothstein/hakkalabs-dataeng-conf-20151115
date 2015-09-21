import Common._
import sbt.complete.Parsers._

lazy val root = Project("movielens", file(".")).
  settings(commonSettings: _*).
  settings({
    val getData = inputKey[Set[File]]("Get 100k movielens data")
    getData := {
      val args: Seq[String] = spaceDelimited("<url>").parsed
      val url = new URL(args.headOption.getOrElse("http://files.grouplens.org/datasets/movielens/ml-100k.zip"))
      IO.unzipURL(url, baseDirectory.value, NameFilter.fnToNameFilter(x => x matches ".*(u.data|u.item)"))
    }
  }).
  aggregate(core, vis, ml)

lazy val core = movielensProject("movielens-core").
  settings(Dependencies.core)

lazy val vis = movielensProject("movielens-vis").
  dependsOn(core).
  settings(Dependencies.vis).
  settings(SparkSubmit.common: _*).
  settings(SparkSubmit.vis: _*)

lazy val ml = movielensProject("movielens-ml").
  dependsOn(core).
  settings(Dependencies.ml).
  settings(SparkSubmit.common: _*).
  settings(SparkSubmit.ml: _*)
