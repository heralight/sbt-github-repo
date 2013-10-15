package com.github.suikwasha

trait GitClient {
  def commit(message: String): Boolean
  def createBranch(name: String): Boolean
  def listBranches(): Seq[String]
  def push(): Boolean
}

trait GitClientProvider {
  def newClient(file: sbt.File): GitClient
}
