import sbt._

scalaVersion := "2.12.4"

scalacOptions ++= Seq(
  "-deprecation"
  , "-unchecked"
  , "-encoding", "UTF-8"
  , "-Xverify"
  , "-feature"
  , "-language:postfixOps"
)

val FinchVersion = "0.16.0-M1" // todo bump version and migrate
val CirceVersion = "0.8.0"
val akkaVersion = "2.5.9"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",

  "com.github.finagle" %% "finch-core" % FinchVersion,
  "com.github.finagle" %% "finch-circe" % FinchVersion,

  "io.circe" %% "circe-core" % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion,

  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)

fork := true

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
)

resolvers ++= Seq(
  "TM" at "http://maven.twttr.com",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Secured Central Repository" at "https://repo1.maven.org/maven2",
  Resolver.sonatypeRepo("snapshots")
)

javaOptions in reStart ++= Seq("-XX:+UseConcMarkSweepGC","-Xmx4g", "-Xms1g")