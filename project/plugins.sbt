resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "Hexx releases" at "http://hexx.fever.io/maven/releases"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")
