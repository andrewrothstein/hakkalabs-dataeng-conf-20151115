import sbt._
import Keys._

object Dependencies {
  val Versions = Seq(
    crossScalaVersions := Seq("2.10.5", "2.11.6"),
    scalaVersion := crossScalaVersions.value.head
  )

  object Compile {
    val spark_core = "org.apache.spark" %% "spark-core" % "1.5.0" % "provided"
    val spark_sql = "org.apache.spark" %% "spark-sql" % "1.5.0" % "provided"
    val spark_mllib = "org.apache.spark" %% "spark-mllib" % "1.5.0" % "provided"

    val scopt = "com.github.scopt" %% "scopt" % "3.3.0"
    val json4sNative = "org.json4s" %% "json4s-native" % "3.2.11"

    object Test {
      val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    }
  }

  import Compile._
  private[this] val l = libraryDependencies

  val core = l ++= Seq(spark_core, Test.scalatest)
  val ml = core ++ (l ++= Seq(spark_mllib, scopt))
  val vis = core ++ (l ++= Seq(spark_sql, json4sNative))
}