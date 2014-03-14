# a fork of sbt-github-repo

creates your own Maven/ivy repository on Github.

## Installation

Add the following to `project/plugins.sbt` or `~/.sbt/plugins/plugins.sbt` file:

    resolvers += "sbt-github-repo" at "http://sbt-github-repo.fever.ch"

    addSbtPlugin("ch.fever" % "sbt-githubrepo-plugin" % "0.1.1")

## Publishing

If you want to create a repository on `git@github.com/newhoggy/repo`

Run `sbt publish-ghrepo git@github.com/path/to/your/repo` to publish your artifacts.
plugin creates branch `gh-pages` automatically.
