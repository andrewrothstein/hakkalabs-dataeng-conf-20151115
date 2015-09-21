import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtsparksubmit.SparkSubmitPlugin.autoImport._

object SparkSubmit {
  lazy val common = Seq(
    //fork in run := true,
    test in assembly := {},
    sparkSubmitJar := assembly.value.getAbsolutePath
  )

  lazy val ml =
    SparkSubmitSetting(
      SparkSubmitSetting("sparkMovieLensALS",
        Seq(
          "--class", "MovieLensALS"
        ),
        Seq(
          "--numIterations", "20",
          "ml-100k/u.data"
        )
      ),
      SparkSubmitSetting("sparkMovieLensALSQuick",
        Seq(
          "--class", "MovieLensALS"
        ),
        Seq(
          "--numIterations", "1",
          "ml-100k/u.data"
        )
      )
    )

  lazy val vis = {
    val task = SparkSubmitSetting("sparkMovieLensForce",
      Seq(
        "--class", "MovieLensForce"
      ), Seq()
    )
    task.settings(
      sparkSubmitAppArgs in task := {
        Seq(
          "ml-100k/u.data",
          "ml-100k/u.item",
          sourceDirectory.value / "main" / "html" getAbsolutePath
        )
      }
    )
  }
}
