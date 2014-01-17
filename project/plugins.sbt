resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "Hexx releases" at "http://hexx.github.io/maven/releases"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")
