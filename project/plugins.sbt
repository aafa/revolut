resolvers ++= Seq(
    Classpaths.typesafeReleases,
    Classpaths.sbtPluginReleases,
    "jgit-repo" at "http://download.eclipse.org/jgit/maven",
    "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
  )

addSbtPlugin("io.spray"               % "sbt-revolver"           % "0.9.0")

addSbtPlugin("io.verizon.build"       % "sbt-rig"                % "3.0.+")

addSbtPlugin("pl.project13.scala"     % "sbt-jmh"                % "0.2.27")

addSbtPlugin("org.scalastyle"         %% "scalastyle-sbt-plugin" % "1.0.0")
