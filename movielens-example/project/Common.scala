import sbt._
import Keys._

import scala.language.experimental.macros
import scala.reflect.macros.Context

object Common {
  val commonSettings = Seq(
    organization in ThisBuild := "com.github.saurfang",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation", "-feature"),
    parallelExecution in test := false,
    updateOptions := updateOptions.value.withCachedResolution(true)
  )

  def movielensProject(path: String): Project = macro movielensProjectMacroImpl

  def movielensProjectMacroImpl(c: Context)(path: c.Expr[String]) = {
    import c.universe._
    reify {
      (Project.projectMacroImpl(c).splice in file(path.splice)).
        settings(name := path.splice).
        settings(Dependencies.Versions).
        settings(commonSettings: _*)
    }
  }
}