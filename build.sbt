seq(githubRepoSettings: _*)

sbtPlugin := true

localRepo := baseDirectory.value

githubRepo := "git@github.com:suikwasha/sbt-github-repo.git"

organization := "com.github.hexx"

name := "sbt-github-repo"

version := "0.1.0"

scalaVersion := "2.10.0"

crossScalaVersions <<= sbtVersion(v => v match {
  case v013 if v013.startsWith("0.13") => Seq("2.10.0")
  case _ => Seq("2.10.0", "2.9.3")
})

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

libraryDependencies <++= (scalaVersion, sbtBinaryVersion) { (scalaV, sbtV) => scalaV match {
  case v210 if v210.startsWith("2.10") =>
	Seq("com.typesafe.sbt" % "sbt-git" % "0.6.2" extra("scalaVersion" -> "2.10", "sbtVersion" -> sbtV))
  case _ =>
	Seq("com.typesafe.sbt" % "sbt-git" % "0.6.2" extra("scalaVersion" -> scalaV, "sbtVersion" -> sbtV))
}}

publishMavenStyle := false
