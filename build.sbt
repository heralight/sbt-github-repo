sbtPlugin := true

organization := "com.github.suikwasha"

name := "sbt-github-repo"

useJGit

version := "0.1.0"

crossScalaVersions := Seq("2.10.1", "2.9.3", "2.9.2")

libraryDependencies <++= (scalaVersion, sbtBinaryVersion) { (scalaV, sbtV) => Seq(
  "com.typesafe.sbt" % "sbt-git" % "0.6.2" extra("scalaVersion" -> scalaV, "sbtVersion" -> sbtV),
  "commons-io" % "commons-io" % "2.0",
  "org.specs2" %% "specs2" % "1.12.4.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)}

publishMavenStyle := false
