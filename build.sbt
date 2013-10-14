sbtPlugin := true

organization := "com.github.suikwasha"

name := "sbt-githubrepo-plugin"

version := "0.1.1"

crossScalaVersions := Seq("2.10.1", "2.9.3", "2.9.2")

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "2.2.0.201212191850-r",
  "org.specs2" %% "specs2" % "1.12.4.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)
