import sbt._
import sbt.Keys._

object RevolutBuild extends Build {

  lazy val revolut = Project(
    id = "revolut",
    base = file("."),
    settings = Seq(
      name := "revolut",
      organization := "aafa",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.12.3"
    )
  )
}
